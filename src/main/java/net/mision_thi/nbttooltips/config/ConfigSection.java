package net.mision_thi.nbttooltips.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LazilyParsedNumber;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public class ConfigSection {
    public static final Data<ConfigSection> SECTION = new ClassicGetter<>(ConfigSection::new, ConfigSection::getRawElement);
    public static final Data<Boolean> BOOLEAN = new ClassicGetter<>(JsonElement::getAsBoolean, JsonPrimitive::new);
    public static final Data<String> STRING = new ClassicGetter<>(JsonElement::getAsString, JsonPrimitive::new);
    public static final Data<Integer> INTEGER = new ClassicGetter<>(JsonElement::getAsInt, JsonPrimitive::new);
    public static final Data<Long> LONG = new ClassicGetter<>(JsonElement::getAsLong, JsonPrimitive::new);
    public static final Data<Float> FLOAT = new ClassicGetter<>(JsonElement::getAsFloat, JsonPrimitive::new);
    public static final Data<Double> DOUBLE = new ClassicGetter<>(JsonElement::getAsDouble, JsonPrimitive::new);
    public static final Data<Byte> BYTE = new ClassicGetter<>(JsonElement::getAsByte, JsonPrimitive::new);
    public static final Data<String[]> STRING_ARRAY = new ClassicGetter<>(element1 -> {
        assert element1.isJsonArray();
        JsonArray array = element1.getAsJsonArray();
        String[] stringArray = new String[array.size()];
        int i = 0;
        for (JsonElement el : array) {
            stringArray[i] = el.getAsString();
            i++;
        }
        return stringArray;
    }, strings -> {
        JsonArray array = new JsonArray(strings.length);
        for (String s : strings)
            array.add(new JsonPrimitive(s));
        return array;
    });

    public static final Data<UUID> UUID = new ClassicGetter<>(jsonElement -> java.util.UUID.fromString(jsonElement.getAsString()), uuid -> new JsonPrimitive(uuid.toString()));

    public static ConfigSection empty() {
        return new ConfigSection(new JsonObject());
    }

    public static ConfigSection emptyArray() {
        return new ConfigSection(new JsonArray());
    }

    protected JsonElement element;

    public ConfigSection(JsonElement base) {
        element = base;
    }

    public <T> T get(String key, ConfigFile.Data<T> data) {
        if (element == null) element = new JsonObject();
        return data.get(element, key);
    }

    public <T> T get(String key, ConfigFile.Data<T> data, T def) {
        if (element == null) element = new JsonObject();
        if (!has(key)) return def;
        return data.get(element, key);
    }

    public <T> T getOrSetDefault(String key, ConfigFile.Data<T> data, T def) {
        if (element == null) element = new JsonObject();
        if (!has(key)) {
            set(key, def, data);
            return def;
        }
        return data.get(element, key);
    }

    public <T> void set(String key, T value, ConfigFile.Data<T> type) {
        if (element == null) element = new JsonObject();
        if (value == null) {
            if (element instanceof JsonObject object)
                if (object.has(key))
                    object.remove(key);
            return;
        }
        type.set(element, key, value);
    }

    public <T> T as(ConfigFile.Data<T> data) {
        if (element == null) element = new JsonObject();
        return data.get(element, null);
    }

    public boolean has(String key) {
        if (element == null) element = new JsonObject();
        return element.getAsJsonObject().has(key);
    }

    public JsonElement getRawElement() {
        return element;
    }

    public void setRawElement(JsonElement element) {
        this.element = element;
    }

    public void forEach(Consumer<ConfigSection> elementConsumer) {
        if (element == null) element = new JsonArray();
        element.getAsJsonArray().forEach(element1 -> {
            elementConsumer.accept(new ConfigSection(element1));
        });
    }

    public void forEntries(BiConsumer<String, ConfigSection> entryConsumer) {
        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().asMap().entrySet()) {
            entryConsumer.accept(entry.getKey(), new ConfigSection(entry.getValue()));
        }
    }

    public interface Data<T> {
        T get(JsonElement element, String key);

        void set(JsonElement element, String key, T data);
    }

    public static class ClassicGetter<T> implements Data<T> {
        private final Function<JsonElement, T> fun;
        private final Function<T, JsonElement> elementBuilder;

        public ClassicGetter(Function<JsonElement, T> tFunction, Function<T, JsonElement> elementBuilder) {
            this.fun = tFunction;
            this.elementBuilder = elementBuilder;
        }

        @Override
        public T get(JsonElement element, String key) {
            if (key == null) {
                return fun.apply(element);
            }
            JsonElement element1 = element.getAsJsonObject().get(key);
            if (element1 == null) return null;
            return fun.apply(element1);
        }

        @Override
        public void set(JsonElement element, String key, T data) {
            if (element.isJsonArray())
                element.getAsJsonArray().add(elementBuilder.apply(data));
            else
                element.getAsJsonObject().add(key, elementBuilder.apply(data));
        }
    }

}
