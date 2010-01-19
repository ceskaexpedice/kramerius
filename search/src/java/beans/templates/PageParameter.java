package beans.templates;

public class PageParameter { 
	private String content, direct;

	public void setContent(String s) {content = s; }
	public void setDirect(String s) { direct = s; }

	public String getContent() { return content;}
	public boolean isDirect() { return Boolean.valueOf(direct).booleanValue(); }

	public PageParameter(String content, String direct) {
		this.content = content;
		this.direct = direct;
	}
}
