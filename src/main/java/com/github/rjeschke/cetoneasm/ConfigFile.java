/*
 * Copyright (C) 2015 René Jeschke <rene_jeschke@yahoo.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.rjeschke.cetoneasm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public final class ConfigFile
{
    private final HashMap<String, Section>        sections = new HashMap<String, Section>();
    private final boolean                         lowercaseKeys;
    private final boolean                         readOnly;
    private final static HashMap<String, Boolean> BOOLEAN_MAP;

    static
    {
        BOOLEAN_MAP = new HashMap<String, Boolean>();

        BOOLEAN_MAP.put("on", Boolean.TRUE);
        BOOLEAN_MAP.put("true", Boolean.TRUE);
        BOOLEAN_MAP.put("enable", Boolean.TRUE);

        BOOLEAN_MAP.put("off", Boolean.FALSE);
        BOOLEAN_MAP.put("false", Boolean.FALSE);
        BOOLEAN_MAP.put("disable", Boolean.FALSE);
    }

    public ConfigFile()
    {
        this(true, true);
    }

    public ConfigFile(final boolean lowercaseKeys, final boolean readOnly)
    {
        this.lowercaseKeys = lowercaseKeys;
        this.readOnly = readOnly;
    }

    public ConfigFile load(final File file) throws IOException
    {
        final StringBuilder sb = new StringBuilder();
        String line;
        int lineNo = 0;
        Section sect = new Section("");

        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        try
        {
            while ((line = reader.readLine()) != null)
            {
                lineNo++;

                line = line.trim();

                if (stringIsEmpty(line) || line.charAt(0) == '#')
                {
                    continue;
                }

                if (line.charAt(0) == '[')
                {
                    sb.setLength(0);
                    this.scan(line, 1, sb, ']');

                    final String s = this.preprocessKey(sb.toString());

                    if (!isValidSection(s))
                    {
                        throw new IOException("Illegal section name");
                    }

                    if (!sect.isEmpty() && !this.sections.containsKey(sect.name))
                    {
                        this.sections.put(sect.name, sect);
                    }

                    if (!sect.name.equals(s))
                    {
                        if (this.sections.containsKey(s))
                        {
                            sect = this.sections.get(s);
                        }
                        else
                        {
                            sect = new Section(s);
                        }
                    }
                }
                else
                {
                    sb.setLength(0);
                    final int sep = this.scan(line, 0, sb, '=');
                    final String key = this.preprocessKey(sb.toString());
                    if (!isValidKey(key))
                    {
                        throw new IOException("Illegal key name");
                    }

                    sb.setLength(0);
                    this.scan(line, sep + 1, sb);
                    final String value = sb.toString().trim();

                    if (key.length() == 0)
                    {
                        throw new IOException("Missing key");
                    }

                    if (!value.isEmpty())
                    {
                        sect.entries.put(key, value);
                    }
                }
            }

        }
        catch (final IOException e)
        {
            throw new IOException(e.getMessage() + " in '" + file.getAbsolutePath() + "' at line " + lineNo);
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (final IOException e)
                {
                    //
                }
            }
        }

        if (!sect.isEmpty() && !this.sections.containsKey(sect.name))
        {
            this.sections.put(sect.name, sect);
        }

        return this;
    }

    public boolean hasSection(final String name)
    {
        return this.sections.containsKey(this.preprocessKey(name));
    }

    public boolean hasKey(final String section, final String key)
    {
        final Section s = this.sections.get(this.preprocessKey(section));
        return s != null && s.entries.containsKey(this.preprocessKey(key));
    }

    public boolean hasKey(final String compoundKey)
    {
        final int idx = compoundKey.lastIndexOf('.');
        final String sect, key;
        if (idx >= -1)
        {
            sect = compoundKey.substring(0, idx);
            key = compoundKey.substring(idx + 1);
        }
        else
        {
            sect = "";
            key = compoundKey;
        }

        return this.hasKey(sect, key);
    }

    public List<Entry<String, String>> getAll()
    {
        final List<Entry<String, String>> ret = new ArrayList<Entry<String, String>>();

        for (final String sk : this.sections.keySet())
        {
            final Section sect = this.sections.get(sk);

            for (final String key : sect.entries.keySet())
            {
                if (!sect.name.isEmpty())
                {
                    ret.add(new ConfigEntry(sect.name + "." + key, this));
                }
                else
                {
                    ret.add(new ConfigEntry(key, this));
                }
            }
        }

        return ret;
    }

    public List<Entry<String, String>> getAll(final String section)
    {
        final List<Entry<String, String>> ret = new ArrayList<Entry<String, String>>();

        final Section sect = this.sections.get(this.preprocessKey(section));

        if (sect != null)
        {
            for (final String key : sect.entries.keySet())
            {
                if (!sect.name.isEmpty())
                {
                    ret.add(new ConfigEntry(sect.name + "." + key, this));
                }
                else
                {
                    ret.add(new ConfigEntry(key, this));
                }
            }
        }

        return ret;
    }

    public String get(final String section, final String key, final String defaultValue)
    {
        final Section s = this.sections.get(this.preprocessKey(section));
        return s != null ? s.entries.get(this.preprocessKey(key)) : defaultValue;
    }

    public String get(final String section, final String key)
    {
        return this.get(section, key, null);
    }

    public boolean getBoolean(final String section, final String key, final boolean defaultValue)
    {
        final String v = this.get(section, key);
        return stringIsEmpty(v) ? defaultValue : BOOLEAN_MAP.get(v.toLowerCase()).booleanValue();
    }

    public boolean getBoolean(final String section, final String key)
    {
        return this.getBoolean(section, key, false);
    }

    public long getLong(final String section, final String key, final long defaultValue)
    {
        final String v = this.get(section, key);
        return stringIsEmpty(v) ? defaultValue : Long.parseLong(v);
    }

    public long getLong(final String section, final String key)
    {
        return this.getLong(section, key, 0);
    }

    public int getInt(final String section, final String key, final int defaultValue)
    {
        final String v = this.get(section, key);
        return stringIsEmpty(v) ? defaultValue : Integer.parseInt(v);
    }

    public int getInt(final String section, final String key)
    {
        return this.getInt(section, key, 0);
    }

    public double getDouble(final String section, final String key, final double defaultValue)
    {
        final String v = this.get(section, key);
        return stringIsEmpty(v) ? defaultValue : Double.parseDouble(v);
    }

    public double getDouble(final String section, final String key)
    {
        return this.getDouble(section, key, 0);
    }

    public float getFloat(final String section, final String key, final float defaultValue)
    {
        final String v = this.get(section, key);
        return stringIsEmpty(v) ? defaultValue : Float.parseFloat(v);
    }

    public float getFloat(final String section, final String key)
    {
        return this.getFloat(section, key, 0);
    }

    public String get(final String compoundKey)
    {
        final int idx = compoundKey.lastIndexOf('.');
        final String sect, key;
        if (idx >= -1)
        {
            sect = compoundKey.substring(0, idx);
            key = compoundKey.substring(idx + 1);
        }
        else
        {
            sect = "";
            key = compoundKey;
        }

        return this.get(sect, key);
    }

    public boolean getBoolean(final String compoundKey, final boolean defaultValue)
    {
        final String v = this.get(compoundKey);
        return stringIsEmpty(v) ? defaultValue : BOOLEAN_MAP.get(v.toLowerCase()).booleanValue();
    }

    public boolean getBoolean(final String compoundKey)
    {
        return this.getBoolean(compoundKey, false);
    }

    public long getLong(final String compoundKey, final long defaultValue)
    {
        final String v = this.get(compoundKey);
        return stringIsEmpty(v) ? defaultValue : Long.parseLong(v);
    }

    public long getLong(final String compoundKey)
    {
        return this.getLong(compoundKey, 0);
    }

    public int getInt(final String compoundKey, final int defaultValue)
    {
        final String v = this.get(compoundKey);
        return stringIsEmpty(v) ? defaultValue : Integer.parseInt(v);
    }

    public int getInt(final String compoundKey)
    {
        return this.getInt(compoundKey, 0);
    }

    public double getDouble(final String compoundKey, final double defaultValue)
    {
        final String v = this.get(compoundKey);
        return stringIsEmpty(v) ? defaultValue : Double.parseDouble(v);
    }

    public double getDouble(final String compoundKey)
    {
        return this.getDouble(compoundKey, 0);
    }

    public float getFloat(final String compoundKey, final float defaultValue)
    {
        final String v = this.get(compoundKey);
        return stringIsEmpty(v) ? defaultValue : Float.parseFloat(v);
    }

    public float getFloat(final String compoundKey)
    {
        return this.getFloat(compoundKey, 0);
    }

    public String put(final String section, final String key, final String value)
    {
        if (this.readOnly)
        {
            throw new IllegalStateException("This Configuration instance is read only.");
        }

        final String sectionName = this.preprocessKey(section);
        Section s = this.sections.get(this.preprocessKey(section));
        if (s == null)
        {
            s = new Section(sectionName);
            this.sections.put(sectionName, s);
        }

        final String keyName = this.preprocessKey(key);
        final String old = s.entries.get(keyName);
        s.entries.put(keyName, value);

        return old;
    }

    public String put(final String section, final String key, final boolean value)
    {
        return this.put(section, key, value ? "true" : "false");
    }

    public String put(final String section, final String key, final long value)
    {
        return this.put(section, key, Long.toString(value));
    }

    public String put(final String section, final String key, final int value)
    {
        return this.put(section, key, Integer.toString(value));
    }

    public String put(final String section, final String key, final double value)
    {
        return this.put(section, key, Double.toString(value));
    }

    public String put(final String section, final String key, final float value)
    {
        return this.put(section, key, Float.toString(value));
    }

    public String put(final String compoundKey, final String value)
    {
        final int idx = compoundKey.lastIndexOf('.');
        final String sect, key;
        if (idx >= -1)
        {
            sect = compoundKey.substring(0, idx);
            key = compoundKey.substring(idx + 1);
        }
        else
        {
            sect = "";
            key = compoundKey;
        }

        return this.put(sect, key, value);
    }

    public String put(final String compoundKey, final boolean value)
    {
        return this.put(compoundKey, value ? "true" : "false");
    }

    public String put(final String compoundKey, final long value)
    {
        return this.put(compoundKey, Long.toString(value));
    }

    public String put(final String compoundKey, final int value)
    {
        return this.put(compoundKey, Integer.toString(value));
    }

    public String put(final String compoundKey, final double value)
    {
        return this.put(compoundKey, Double.toString(value));
    }

    public String put(final String compoundKey, final float value)
    {
        return this.put(compoundKey, Float.toString(value));
    }

    public List<String> getSections()
    {
        final List<String> ret = new ArrayList<String>();

        for (final String sk : this.sections.keySet())
        {
            ret.add(sk);
        }

        Collections.sort(ret);
        return ret;
    }

    public List<String> getKeys()
    {
        final List<String> ret = new ArrayList<String>();

        for (final String sk : this.sections.keySet())
        {
            final Section sect = this.sections.get(sk);

            for (final String key : sect.entries.keySet())
            {
                if (!sect.name.isEmpty())
                {
                    ret.add(sect.name + "." + key);
                }
                else
                {
                    ret.add(key);
                }
            }
        }

        Collections.sort(ret);
        return ret;
    }

    public void checkConstraints(final List<Constraint> constraints) throws IOException
    {
        for (final Constraint c : constraints)
        {
            c.check(this);
        }
    }

    public void checkConstraints(final Constraint... constraints) throws IOException
    {
        for (final Constraint c : constraints)
        {
            c.check(this);
        }
    }

    private String preprocessKey(final String key)
    {
        String ret = key;

        if (key.length() > 0)
        {
            if (this.lowercaseKeys)
            {
                ret = key.toLowerCase();
            }

            if (Character.isWhitespace(ret.charAt(0)) || Character.isWhitespace(ret.charAt(key.length() - 1)))
            {
                ret = ret.trim();
            }
        }

        return ret;
    }

    private static boolean isValidKey(final String k)
    {
        if (stringIsEmpty(k))
        {
            return false;
        }

        for (int i = 0; i < k.length(); i++)
        {
            final char c = k.charAt(i);

            if (Character.isSpaceChar(c) || Character.isWhitespace(c))
            {
                return false;
            }

            switch (c)
            {
            case '.':
                return false;
            }
        }

        return true;
    }

    private static boolean isValidSection(final String k)
    {
        if (stringIsEmpty(k))
        {
            return false;
        }

        if (k.charAt(0) == '.' || k.charAt(k.length() - 1) == '.')
        {
            return false;
        }

        for (int i = 0; i < k.length(); i++)
        {
            final char c = k.charAt(i);

            if (Character.isSpaceChar(c) || Character.isWhitespace(c))
            {
                return false;
            }
        }

        return true;
    }

    private int scan(final String line, final int index, final StringBuilder sb) throws IOException
    {
        return this.scan(line, index, sb, '\0');
    }

    private int scan(final String line, final int index, final StringBuilder sb, final char end) throws IOException
    {
        int p = index;
        while (p < line.length() && (end == '\0' || line.charAt(p) != end))
        {
            final char c = line.charAt(p++);
            if (c == '\\')
            {
                final char next = p < line.length() ? line.charAt(p++) : '\0';
                switch (next)
                {
                case 'r':
                    sb.append('\r');
                    break;
                case 'n':
                    sb.append('\n');
                    break;
                case 't':
                    sb.append('\t');
                    break;
                case '#':
                    sb.append('#');
                    break;
                case '=':
                    sb.append('=');
                    break;
                case '\\':
                    sb.append('\\');
                    break;
                default:
                    throw new IOException("Illegal escape sequence");
                }
            }
            else
            {
                if (c == '#')
                {
                    if (end != '\0')
                    {
                        throw new IOException("Unexpected comment");
                    }
                    break;
                }
                sb.append(c);
            }
        }

        if (end != '\0')
        {
            if (p >= line.length() || line.charAt(p) != end)
            {

                throw new IOException("'" + end + "' expected");
            }
        }

        return p;
    }

    @Override
    public String toString()
    {
        return this.getAll().toString();
    }

    public static final class Constraint
    {
        private final String  key;
        private final Type    type;
        private final boolean optional;

        public Constraint(final String key, final Type type, final boolean optional)
        {
            this.key = key;
            this.type = type;
            this.optional = optional;
        }

        public Constraint(final String key, final Type type)
        {
            this(key, type, false);
        }

        public static Constraint of(final String key, final Type type, final boolean optional)
        {
            return new Constraint(key, type, optional);
        }

        public static Constraint of(final String key, final Type type)
        {
            return new Constraint(key, type);
        }

        private void check(final ConfigFile config) throws IOException
        {
            final String value = config.get(this.key);
            if (stringIsEmpty(value))
            {
                if (!this.optional)
                {
                    throw new IOException("Missing mandatory entry for key '" + this.key + "'");
                }
            }
            else
            {
                switch (this.type)
                {
                case STRING:
                    return;
                case BOOLEAN:
                    if (!ConfigFile.BOOLEAN_MAP.containsKey(value.toLowerCase()))
                    {
                        throw new IOException("Illegal value for key '" + this.key + "', boolean value expected");
                    }
                    return;
                case INTEGER:
                    try
                    {
                        Integer.parseInt(value);
                    }
                    catch (final NumberFormatException e)
                    {
                        throw new IOException("Illegal value for key '" + this.key + "', integer expected");
                    }
                    return;
                case LONG:
                    try
                    {
                        Long.parseLong(value);
                    }
                    catch (final NumberFormatException e)
                    {
                        throw new IOException("Illegal value for key '" + this.key + "', long expected");
                    }
                    return;
                case FLOAT:
                    try
                    {
                        Float.parseFloat(value);
                    }
                    catch (final NumberFormatException e)
                    {
                        throw new IOException("Illegal value for key '" + this.key + "', float expected");
                    }
                    return;
                case DOUBLE:
                    try
                    {
                        Double.parseDouble(value);
                    }
                    catch (final NumberFormatException e)
                    {
                        throw new IOException("Illegal value for key '" + this.key + "', double expected");
                    }
                    return;
                }
            }
        }

        public enum Type
        {
            STRING,
            BOOLEAN,
            INTEGER,
            LONG,
            FLOAT,
            DOUBLE
        }

        @Override
        public String toString()
        {
            if (this.optional)
            {
                return "Optional key '" + this.key + "' of type " + this.type;
            }
            return "Mandatory key '" + this.key + "' of type " + this.type;
        }
    }

    private static class ConfigEntry implements Entry<String, String>
    {
        private final String     key;
        private final ConfigFile config;

        public ConfigEntry(final String key, final ConfigFile config)
        {
            this.key = key;
            this.config = config;
        }

        @Override
        public String getKey()
        {
            return this.key;
        }

        @Override
        public String getValue()
        {
            return this.config.get(this.key);
        }

        @Override
        public String setValue(final String value)
        {
            return this.config.put(this.key, value);
        }

        @Override
        public String toString()
        {
            return this.getKey() + "=" + this.getValue();
        }
    }

    static class Section
    {
        public final String                  name;
        public final HashMap<String, String> entries = new HashMap<String, String>();

        public Section(final String name)
        {
            this.name = name;
        }

        public boolean isEmpty()
        {
            return this.entries.isEmpty();
        }
    }

    private final static boolean stringIsEmpty(final String str)
    {
        return str == null || str.length() == 0;
    }
}
