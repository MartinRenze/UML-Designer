package addebugger;

import java.util.HashMap;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;

public class DebuggerService {

	public static DebuggerService INSTANCE;

	private static final String TOKEN_STEREOTYPE_NAME = "Token";
	private static final String TOKEN_PROPERTY_NUMBER = "number";

	private Stereotype token;
	private boolean initialized = false;

	private Thread serverThread;
	private DebuggerServer server;

	private final int port = 20000;

	private String context;

	private final HashMap<String, NamedElement> elements = new HashMap<String, NamedElement>();

	private final HashMap<String, String> tokens = new HashMap<String, String>();

	public TokenMessage getAction(String string) {
		if (string.startsWith("SET_TOKEN")) {
			String node = "";
			String tokenID = "";
			int count = 0;
			for (final String sub : string.split(";")) {
				if (sub.startsWith("NODE")) {
					node = sub.substring("NODE:".length());
				} else if (sub.startsWith("CURRENT_TOKENS")) {
					count = Integer.decode(sub.substring("CURRENT_TOKENS:".length()));
				} else if (sub.startsWith("TOKEN")) {
					tokenID = sub.substring("TOKEN:".length());
				}
			}
			if (!node.isEmpty() || !tokenID.isEmpty()) {
				return new TokenMessage(node, tokenID, count);
			}
		}
		return null;
	}

	public void handle(String text) {

		final TokenMessage message = getAction(text);
		if (message != null) {
			String qualifiedName = message.getNodeName();
			if (message.getTokenCount() == 0 && tokens.containsKey(message.getTokenID())) {
				qualifiedName = tokens.get(message.getTokenID());

				System.out.println("Message Accepted, setting Token Count for <" + qualifiedName + "> to: "
						+ message.getTokenCount());

				TransactionUtil.getEditingDomain(elements.get(qualifiedName)).getCommandStack()
				.execute(new DebuggerCommand(elements.get(qualifiedName), message.getTokenCount()));
				tokens.remove(message.getTokenID());
			} else {
				if (tokens.containsKey(message.getTokenID())) {
					final String oldOwner = tokens.get(message.getTokenID());
					if (!oldOwner.equals(message.getNodeName())) {
						TransactionUtil.getEditingDomain(elements.get(oldOwner)).getCommandStack()
						.execute(new DebuggerCommand(elements.get(oldOwner), 0));
						tokens.remove(message.getTokenID());
					}
				}

				if (elements.containsKey(qualifiedName)) {
					System.out.println("Message Accepted, setting Token Count for <" + qualifiedName
							+ "> to: " + message.getTokenCount());

					final TransactionalEditingDomain domain = TransactionUtil
							.getEditingDomain(elements.get(qualifiedName));
					if (domain == null) {
						System.out.println("ERROR: Domain for <" + qualifiedName + "> is null!");
						return;
					}
					domain.getCommandStack().execute(
							new DebuggerCommand(elements.get(qualifiedName), message.getTokenCount()));
					tokens.put(message.getTokenID(), qualifiedName);
				} else {
					System.out.println(
							"Message Discarted, Element " + qualifiedName + " does not accept Tokens");
				}
			}
			System.out.println(
					"----------------------------------------------------------------------------------------------");
		}
	}

	public boolean hasStereotype(Element element) {
		for (final Stereotype type : element.getApplicableStereotypes()) {
			if (type.getName().equals(TOKEN_STEREOTYPE_NAME)) {
				return true;
			}
		}
		return false;
	}

	public Activity init(Element context){
		if (INSTANCE == null) {
			INSTANCE = this;
		}
		for (final Element element : context.getNearestPackage().allOwnedElements()) {
			if (element instanceof NamedElement) {
				initSterotype(element.getApplicableStereotypes());
				if (hasStereotype(element)) { // TODO decide to filter (qualified Name)
					elements.put(((NamedElement)element).getQualifiedName(), (NamedElement)element);
					System.out.println("Added Element <" + ((NamedElement)element).getQualifiedName()
							+ "> to Applicable Elements");
				}
			}
		}
		return null;
	}

	public void initSterotype(EList<Stereotype> sterotypes) {
		if (!initialized) {
			for (final Stereotype type : sterotypes) {
				if (type.getName().equals(TOKEN_STEREOTYPE_NAME)) {
					token = type;
					initialized = true;
					System.out.println("Token Stereotype was successfully initialized");
					return;
				}
			}
		}
	}

	public Activity selectContext(Action context) {
		System.out.println("Selected: " + context);
		this.context = context.getName();
		return null;
	}

	public void setStereotype(Element element, int tokenCount) {
		if (element instanceof NamedElement) {
			final NamedElement namedElement = (NamedElement)element;
			if (tokenCount <= 0 && element.isStereotypeApplied(token)) {
				element.unapplyStereotype(token);
				System.out.println("Removed Setereotype from <" + namedElement.getQualifiedName() + ">");
			} else if (tokenCount > 0) {
				if (!element.isStereotypeApplied(token)) {
					element.applyStereotype(token);
					System.out.println("Added Stereotype to <" + namedElement.getQualifiedName() + ">");
				}
				for (final Property property : token.getAllAttributes()) {
					if (property.getName().equals(TOKEN_PROPERTY_NUMBER)) {
						element.setValue(token, TOKEN_PROPERTY_NUMBER, tokenCount);
						System.out.println("Token Count for Element <" + namedElement.getQualifiedName()
						+ "> was set to: " + tokenCount);
					}
				}
			}
		}
	}

	public Activity startServer(Element context) {
		if (server == null) {
			System.out.println("server starting");
			server = new DebuggerServer(this, port);
			serverThread = new Thread(server);
			serverThread.start();
		} else {
			System.out.println("server still running");
		}
		return null;
	}

	public Activity stopServer(Element context) {
		if (server != null) {
			server.close();
			serverThread.stop();
			serverThread.destroy();
			serverThread = null;
			server = null;
		}
		return null;
	}
}
