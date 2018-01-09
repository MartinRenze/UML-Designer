package addebugger;

import java.util.Collection;

import org.eclipse.emf.common.command.Command;
import org.eclipse.uml2.uml.Element;

public class DebuggerCommand implements Command {

	private final Element element;

	private final int tokenCount;

	public DebuggerCommand(Element element, int tokenCount) {
		this.element = element;
		this.tokenCount = tokenCount;
	}

	public boolean canExecute() {
		return true;
	}

	public boolean canUndo() {
		return false;
	}

	public Command chain(Command command) {
		return null;
	}

	public void dispose() {

	}

	public void execute() {
		DebuggerService.INSTANCE.setStereotype(element, tokenCount);
	}

	public Collection<?> getAffectedObjects() {
		return null;
	}

	public String getDescription() {
		return "";
	}

	public String getLabel() {
		return "DebuggerCommand";
	}

	public Collection<?> getResult() {
		return null;
	}

	public void redo() {

	}

	public void undo() {

	}

}
