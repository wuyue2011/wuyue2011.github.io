package cn.zbx1425.mtrsteamloco.data;

import net.minecraft.world.level.block.state.properties.Property;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class StringProperty extends Property<String> {
    protected StringProperty(String name) {
        super(name, String.class);
    }

    public static StringProperty create(String name) {
        return new StringProperty(name);
    }

    @Override
    public Collection<String> getPossibleValues() {
        return Collections.emptyList();
    }

    @Override
    public String getName(String value) {
        return value;
    }

    @Override
    public Optional<String> getValue(String name) {
        return Optional.of(name);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass()!= obj.getClass()) {
            return false;
        }
        StringProperty other = (StringProperty) obj;
        return this.getName().equals(other.getName());
    }

    @Override
    public int generateHashCode() {
        return this.getName().hashCode();
    }
}