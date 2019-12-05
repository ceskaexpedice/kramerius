package cz.incad.kramerius.document.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.lowagie.text.Rectangle;

import cz.incad.kramerius.ObjectPidsPath;

/**
 * Represents whole document
 * 
 * @author pavels
 */
public class PreparedDocument extends AbstractObject {

    /** Default WIDTH */
    public static final int DEFAULT_WIDTH = 595;
    /** Default HEIGHT */
    public static final int DEFAULT_HEIGHT = 842;

    // TODO: To remove it !
    private String uuidTitlePage;
    private String uuidFrontCover;
    private String uuidBackCover;
    private String firstPage;
    private String uuidMainTitle;

    protected OutlineItem outlineItemRoot;

    private List<AbstractPage> pages = new ArrayList<AbstractPage>();

    private String documentTitle;

    private int width = DEFAULT_WIDTH;
    private int height = DEFAULT_HEIGHT;

    private ObjectPidsPath objectPidsPath;

    // TODO: Remove
    private Map<String, DCConent> dcs = new HashMap<String, DCConent>();

    public PreparedDocument(String modelName, String uuid) {
        super(modelName, uuid);
        this.outlineItemRoot = new OutlineItem();
    }

    /**
     * REturns title page
     * 
     * @return title page
     */
    public String getUuidTitlePage() {
        return uuidTitlePage;
    }

    /**
     * Sets title page
     * 
     * @param uuidTitlePage
     *            Title page
     */
    public void setUuidTitlePage(String uuidTitlePage) {
        this.uuidTitlePage = uuidTitlePage;
    }

    /**
     * Returns outline root
     * 
     * @return Outline root
     */
    public OutlineItem getOutlineItemRoot() {
        return outlineItemRoot;
    }

    /**
     * Sets new outline root
     * 
     * @param outlineItemRoot
     *            New outline root
     */
    public void setOutlineItemRoot(OutlineItem outlineItemRoot) {
        this.outlineItemRoot = outlineItemRoot;
    }

    /**
     * Returns all pages
     * 
     * @return all pages
     */
    public List<AbstractPage> getPages() {
        return pages;
    }

    /**
     * Sets pages
     * 
     * @param pages
     *            New pages
     */
    public void setPages(List<AbstractPage> pages) {
        this.pages = pages;
    }

    /**
     * Add new page
     * 
     * @param page
     *            New page
     */
    public void addPage(AbstractPage page) {
        this.pages.add(page);
    }

    /**
     * Remove old page
     * 
     * @param page
     *            Old page
     */
    public void removePage(AbstractPage page) {
        this.pages.remove(page);
    }

    @Override
    public void debugInformations(StringBuffer buffer, int level) {
        getOutlineItemRoot().debugInformations(buffer, level + 1);
        for (AbstractPage page : this.pages) {
            page.debugInformations(buffer, level + 1);
        }
    }

    /**
     * Returns document title
     * 
     * @return document title
     */
    public String getDocumentTitle() {
        return documentTitle;
    }

    /**
     * Sets document title
     * 
     * @param documentTitle
     *            Document title
     */
    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    /**
     * Returns main title
     * 
     * @return main title
     */
    @Deprecated
    public String getUuidMainTitle() {
        return uuidMainTitle;
    }

    /**
     * Sets main title
     * 
     * @param uuidMainTitle
     *            main title
     */
    @Deprecated
    public void setUuidMainTitle(String uuidMainTitle) {
        this.uuidMainTitle = uuidMainTitle;
    }

    /**
     * Remove all objects until given one
     * 
     * @param uuid
     *            Specify objects to remove
     */
    public void removePagesTill(String uuid) {
        int pocitadlo = 0;
        while (!this.pages.remove(0).getUuid().equals(uuid)) {
            pocitadlo += 1;
        }
        ;
    }

    /**
     * Returns DC metadata
     * 
     * @param pid
     *            Requested object
     * @return DC content
     */
    public DCConent getDCContent(String pid) {
        return this.dcs.get(pid);
    }

    /**
     * Add new mapping pid to {@link DCConent}
     * 
     * @param pid
     *            PID of the object
     * @param conent
     *            DCContent
     */
    public void mapDCConent(String pid, DCConent conent) {
        this.dcs.put(pid, conent);
    }

    /**
     * Remove mapping
     * 
     * @param pid
     *            Object's pid
     */
    public void unmapDCContent(String pid) {
        this.dcs.remove(pid);
    }

    /**
     * Gets object's pids path
     * 
     * @return Pids path
     */
    public ObjectPidsPath getObjectPidsPath() {
        return objectPidsPath;
    }

    /**
     * Sets the pids path
     * 
     * @param objectPidsPath
     *            New pids path
     */
    public void setObjectPidsPath(ObjectPidsPath objectPidsPath) {
        this.objectPidsPath = objectPidsPath;
    }

    public void divide(OutlineItem leftRoot, OutlineItem rightRoot, String uuid) {
        State state = State.COPY_TO_RIGHT;
        Stack<OutlineItem> stack = new Stack<OutlineItem>();
        stack.push(this.outlineItemRoot);
        while (!stack.isEmpty()) {
            OutlineItem orig = stack.pop();
            OutlineItem[] children = orig.getChildren();
            if (children.length == 0) {
                if (orig.getDestination().equals(uuid)) {
                    state = State.COPY_TO_LEFT;
                }
                if (state == State.COPY_TO_LEFT) {
                    addToCopyRoot(leftRoot, orig);
                } else {
                    addToCopyRoot(rightRoot, orig);
                }
            } else {
                for (OutlineItem chOrig : children) {
                    stack.push(chOrig);
                }
            }
        }
    }

    private void addToCopyRoot(OutlineItem rootCopy, OutlineItem orig) {
        List<String> uuidPath = new ArrayList<String>();
        OutlineItem origParent = orig;
        while (origParent.getParent() != null) {
            uuidPath.add(0, origParent.getDestination());
            origParent = origParent.getParent();
        }
        OutlineItem currentCopy = rootCopy;
        OutlineItem currentOrig = origParent;
        for (String uuid : uuidPath) {
            OutlineItem chCopy = currentCopy.getChild(uuid);
            OutlineItem chOrig = currentOrig.getChild(uuid);
            if (chCopy == null) {
                chCopy = chOrig.copy();
                currentCopy.addChild(0, chCopy);
                chCopy.setParent(currentCopy);
            }
            currentCopy = chCopy;
            currentOrig = chOrig;
        }
    }

    public void removePages() {
        this.pages.clear();
    }

    public static enum State {
        COPY_TO_LEFT, COPY_TO_RIGHT;
    }

    public String getUuidFrontCover() {
        return uuidFrontCover;
    }

    public void setUuidFrontCover(String uuidFrontCover) {
        this.uuidFrontCover = uuidFrontCover;
    }

    public String getUuidBackCover() {
        return uuidBackCover;
    }

    public void setUuidBackCover(String uuidBackCover) {
        this.uuidBackCover = uuidBackCover;
    }

    public String getFirstPage() {
        return firstPage;
    }

    public void setFirstPage(String firstPage) {
        this.firstPage = firstPage;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setRect(int w, int h) {
        this.width = w;
        this.height = h;
    }

    public Rectangle getRectangle() {
        return new Rectangle(this.width, this.height);
    }
}
