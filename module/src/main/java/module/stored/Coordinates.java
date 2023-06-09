package module.stored;

import module.annotations.Fillable;
import module.annotations.Restriction;
import module.annotations.Storable;
import module.annotations.ValidationMode;
import module.utils.ObjectUtils;

import java.io.Serializable;

/**
 * One of the stored objects
 */
public class Coordinates implements Comparable<Coordinates>, Serializable {

    private static final long serialVersionUID = 5629745098267755290L;

    @Fillable
    @Storable
    @Restriction(filter = ValidationMode.TOP_NUMERIC_BOUND, value = 955)
    private long x; //Максимальное значение поля: 955
    @Fillable
    @Storable
    @Restriction(filter = ValidationMode.TOP_NUMERIC_BOUND, value = 207)
    private Integer y; //Максимальное значение поля: 207, Поле не может быть null


    public void setX(long x) {
        this.x = x;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public long getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    @Override
    public int compareTo(Coordinates o) {
        int compare;
        if((compare = ObjectUtils.saveCompare(x, o.x)) != 0)
            return compare;
        else
            return ObjectUtils.saveCompare(y, o.y);
    }

    @Override
    public String toString() {
        return "Coordinates{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}