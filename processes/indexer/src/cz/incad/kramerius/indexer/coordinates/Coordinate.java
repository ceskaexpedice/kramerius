package cz.incad.kramerius.indexer.coordinates;

import java.math.BigDecimal;

public class Coordinate {

    private double coordinate;
    private CoordinationType type;

    public Coordinate(double coordinate, CoordinationType type) {
        this.coordinate = coordinate;
        this.type = type;
    }

    public Coordinate(long degrees, long minutes, long seconds, CoordinationType type) {
        BigDecimal minutesP = new BigDecimal(minutes);
        minutesP = minutesP.divide(new BigDecimal(60), 5, BigDecimal.ROUND_HALF_UP);

        BigDecimal secondsP = new BigDecimal(seconds);
        secondsP = secondsP.divide(new BigDecimal(3600), 5, BigDecimal.ROUND_HALF_UP);

        this.coordinate = degrees + minutesP.doubleValue() + secondsP.doubleValue();
        this.type = type;
        switch (this.type) {
            case JS:
                this.coordinate = this.coordinate * -1;
                break;
            case ZD:
                this.coordinate =this.coordinate *  -1;
                break;
            default:
                break;
        }
    }

    public CoordinationType getType() {
        return type;
    }

    public double getCoordinate() {
        return coordinate;
    }

    static enum CoordinationType {
        VD,
        SS,

        ZD,
        JS
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Coordinate that = (Coordinate) o;

        if (Double.compare(that.coordinate, coordinate) != 0) return false;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(coordinate);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Coordinate{" +
                "coordinate=" + coordinate +
                ", type=" + type +
                '}';
    }
}
