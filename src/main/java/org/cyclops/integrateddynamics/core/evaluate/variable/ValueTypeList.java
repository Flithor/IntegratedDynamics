package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Lists;
import lombok.ToString;
import net.minecraft.util.EnumChatFormatting;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;

import java.util.Iterator;
import java.util.List;

/**
 * Value type with values that are strings.
 * @author rubensworks
 */
public class ValueTypeList extends ValueObjectTypeBase<ValueTypeList.ValueList> {

    public ValueTypeList() {
        super("list", Helpers.RGBToInt(175, 3, 1), EnumChatFormatting.DARK_RED.toString());
    }

    @Override
    public ValueList getDefault() {
        return ValueList.ofList(ValueTypes.CATEGORY_ANY, Lists.<IValue>newArrayList());
    }

    @Override
    public String toCompactString(ValueList value) {
        return value.getRawValue().toCompactString();
    }

    @Override
    public String serialize(ValueList value) {
        try {
            return ValueTypeListProxyFactories.REGISTRY.serialize(value.getRawValue());
        } catch (IValueTypeListProxyFactoryTypeRegistry.SerializationException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public ValueList deserialize(String value) {
        try {
            IValueTypeListProxy<IValueType<IValue>, IValue> proxy = ValueTypeListProxyFactories.REGISTRY.deserialize(value);
            return ValueList.ofFactory(proxy);
        } catch (IValueTypeListProxyFactoryTypeRegistry.SerializationException e) {
            e.printStackTrace();
        }
        return getDefault();
    }

    @Override
    public ValueList materialize(ValueList value) {
        IValueTypeListProxy<IValueType<IValue>, IValue> list = value.getRawValue();
        List<IValue> values = Lists.newArrayListWithExpectedSize(list.getLength());
        for(IValue element : list) {
            values.add(element);
        }
        return ValueList.ofList(list.getValueType(), values);
    }

    @ToString
    public static class ValueList<T extends IValueType<V>, V extends IValue> extends ValueBase {

        private final IValueTypeListProxy<T, V> value;

        private ValueList(IValueTypeListProxy<T, V> value) {
            super(ValueTypes.LIST);
            this.value = value;
        }

        public static <T extends IValueType<V>, V extends IValue> ValueList ofList(T valueType, List<V> values) {
            return new ValueList<>(new ValueTypeListProxyMaterialized<>(valueType, values));
        }

        public static <V extends IValue> ValueList ofAll(V... values) {
            return values.length == 0 ? ValueTypes.LIST.getDefault() : ofList(values[0].getType(), Lists.newArrayList(values));
        }

        public static <T extends IValueType<V>, V extends IValue> ValueList ofFactory(IValueTypeListProxy<T, V> proxy) {
            return new ValueList<>(proxy);
        }

        public IValueTypeListProxy<T, V> getRawValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ValueList && ((ValueList) o).value.equals(this.value);
        }

    }

    public static class ListFactoryIterator<T extends IValueType<V>, V extends IValue> implements Iterator<V> {

        private final IValueTypeListProxy<T, V> value;
        private int index = 0;

        public ListFactoryIterator(IValueTypeListProxy<T, V> value) {
            this.value = value;
        }

        @Override
        public boolean hasNext() {
            return index < value.getLength();
        }

        @Override
        public V next() {
            return value.get(index++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }

}
