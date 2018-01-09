package addebugger;

public class TokenMessage {

	private final String nodeName;

	private final String tokenID;

	private final int tokenCount;

	public TokenMessage(String nodeName, String tokenID, int tokenCount) {
		this.nodeName = nodeName;
		this.tokenID = tokenID;
		this.tokenCount = tokenCount;
	}

	public String getNodeName() {
		return nodeName;
	}

	public int getTokenCount() {
		return tokenCount;
	}

	public String getTokenID() {
		return tokenID;
	}
}
