package module.stored;

import module.annotations.*;
import module.utils.ObjectUtils;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * One of the stored objects
 */
public class Dragon implements Comparable<Dragon>, Serializable {

    private static long instances = 0;

    @Storable
    @Unique
    private long id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    @Storable
    @Fillable
    private String name; //Поле не может быть null, Строка не может быть пустой
    @Complex
    @Fillable
    @Storable
    private Coordinates coordinates; //Поле не может быть null
    @Storable
    private ZonedDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    @Nullable
    @Fillable
    @Storable
    @Restriction(filter = ValidationMode.LOW_NUMERIC_BOUND, value = 1)
    private Long age; //Значение поля должно быть больше 0, Поле может быть null
    @Nullable
    @Fillable
    @Storable
    @Restriction(filter = ValidationMode.LOW_NUMERIC_BOUND, value = 1)
    private Long wingspan; //Значение поля должно быть больше 0, Поле может быть null
    @Fillable
    @Storable
    @Restriction(filter = ValidationMode.LOW_NUMERIC_BOUND, value = 0.001d)
    private float weight; //Значение поля должно быть больше 0
    @Fillable
    @Storable
    private Color color; //Поле не может быть null
    @Nullable
    @Complex
    @Fillable
    @Storable
    private Person killer; //Поле может быть null

    public Dragon() {
        id = UUID.randomUUID().getLeastSignificantBits() & ~(1 << 63);
        creationDate = ZonedDateTime.now();
    }

    @Override
    public int compareTo(Dragon o) {
        int compare;
        if((compare = ObjectUtils.saveCompare(age, o.age)) != 0)
            return compare;
        else if((compare = ObjectUtils.saveCompare(name, o.name)) != 0)
            return compare;
        else if((compare = ObjectUtils.saveCompare(coordinates, o.coordinates)) != 0)
            return compare;
        else if((compare = ObjectUtils.saveCompare(wingspan, o.wingspan)) != 0)
            return compare;
        else if((compare = ObjectUtils.saveCompare(weight, o.weight)) != 0)
            return compare;
        else if((compare = ObjectUtils.saveCompare(color, o.color)) != 0)
            return compare;
        else
            return ObjectUtils.saveCompare(killer, o.killer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dragon dragon = (Dragon) o;
        return id == dragon.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, coordinates, creationDate, age, wingspan, weight, color, killer);
    }

    @Override
    public String toString() {
        return "Dragon{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", coordinates=" + coordinates +
                ", creationDate=" + creationDate.toLocalDate() +
                ", age=" + age +
                ", wingspan=" + wingspan +
                ", weight=" + weight +
                ", color=" + color +
                ", killer=" + killer +
                '}';
    }
}