package cz.incad.kramerius.utils.database;

public class Offset {

    private String offset;
    private String size;
    private String page;

    public Offset(String offset, String size) {
        super();
        this.offset = offset;
        this.size = size;
        Integer.parseInt(this.size);
        Integer.parseInt(this.offset);
    }

    public String getSQLOffset() {
        // return
        // "OFFSET "+this.offset+" ROWS FETCH NEXT "+this.size+" ROWS ONLY";
        return " LIMIT " + this.size + " OFFSET " + this.offset + " ";
    }

    public String getOffset() {
        return offset;
    }

    public String getSize() {
        return size;
    }

    public String getNextOffset() {
        int iOffset = Integer.parseInt(offset);
        int iSize = Integer.parseInt(size);
        return "" + (iOffset + iSize);
    }

    public String getPrevOffset() {
        int iOffset = Integer.parseInt(offset);
        int iSize = Integer.parseInt(size);
        return "" + (iOffset - iSize);
    }

    @Override
    public String toString() {
        return "Offset [offset=" + offset + ", size=" + size + ", page=" + page
                + "]";
    }
    
}
