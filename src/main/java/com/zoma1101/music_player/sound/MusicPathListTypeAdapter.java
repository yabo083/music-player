package com.zoma1101.music_player.sound;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Accepts both:
 * "music": "music/a.ogg"
 * and
 * "music": ["music/a.ogg", "music/b.ogg"]
 */
public class MusicPathListTypeAdapter extends TypeAdapter<List<String>> {

    @Override
    public void write(JsonWriter out, List<String> value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.beginArray();
        for (String item : value) {
            out.value(item);
        }
        out.endArray();
    }

    @Override
    public List<String> read(JsonReader in) throws IOException {
        JsonToken token = in.peek();
        if (token == JsonToken.NULL) {
            in.nextNull();
            return new ArrayList<>();
        }

        List<String> values = new ArrayList<>();
        if (token == JsonToken.STRING) {
            values.add(in.nextString());
            return values;
        }

        if (token == JsonToken.BEGIN_ARRAY) {
            in.beginArray();
            while (in.hasNext()) {
                JsonToken itemToken = in.peek();
                if (itemToken == JsonToken.STRING) {
                    values.add(in.nextString());
                } else if (itemToken == JsonToken.NUMBER) {
                    values.add(in.nextString());
                } else if (itemToken == JsonToken.BOOLEAN) {
                    values.add(Boolean.toString(in.nextBoolean()));
                } else if (itemToken == JsonToken.NULL) {
                    in.nextNull();
                } else {
                    // Unexpected type in list, skip.
                    in.skipValue();
                }
            }
            in.endArray();
            return values;
        }

        // Unexpected shape, consume and return empty list.
        in.skipValue();
        return values;
    }
}
