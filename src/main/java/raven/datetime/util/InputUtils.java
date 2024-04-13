package raven.datetime.util;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class InputUtils extends MaskFormatter {

    private static Map<Component, PropertyChangeListener> inputMap;

    public static LocalTime stringToTime(boolean use24h, String value) {
        try {
            if (use24h) {
                final DateTimeFormatter format24h = DateTimeFormatter.ofPattern("HH:mm");
                return LocalTime.from(format24h.parse(value));
            } else {
                final DateTimeFormatter format12h = DateTimeFormatter.ofPattern("hh:mm a");
                return LocalTime.from(format12h.parse(value.toUpperCase()));
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static LocalDate stringToDate(DateTimeFormatter format, String value) {
        try {
            return LocalDate.from(format.parse(value));
        } catch (Exception e) {
            return null;
        }
    }

    public static LocalDate[] stringToDate(DateTimeFormatter format, String separator, String value) {
        try {
            String[] dates = value.split(separator);
            LocalDate from = LocalDate.from(format.parse(dates[0]));
            LocalDate to = LocalDate.from(format.parse(dates[1]));
            return new LocalDate[]{from, to};
        } catch (Exception e) {
            return null;
        }
    }

    public static void useTimeInput(JFormattedTextField txt, boolean use24h, ValueCallback callback) {
        try {
            removePropertyChange(txt);
            txt.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
            txt.putClientProperty(FlatClientProperties.TEXT_FIELD_CLEAR_CALLBACK, (Consumer<Object>) o -> {
                txt.setValue(null);
                callback.valueChanged(null);
            });
            TimeInputFormat mask = new TimeInputFormat(use24h ? "##:##" : "##:## ??", use24h);
            mask.setCommitsOnValidEdit(true);
            mask.setPlaceholderCharacter('-');
            DefaultFormatterFactory df = new DefaultFormatterFactory(mask);
            txt.setFormatterFactory(df);

            PropertyChangeListener propertyChangeListener = evt -> callback.valueChanged(txt.getValue());
            txt.addPropertyChangeListener("value", propertyChangeListener);
            putPropertyChange(txt, propertyChangeListener);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void useDateInput(JFormattedTextField txt, boolean between, String separator, ValueCallback callback) {
        try {
            removePropertyChange(txt);
            txt.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
            txt.putClientProperty(FlatClientProperties.TEXT_FIELD_CLEAR_CALLBACK, (Consumer<Object>) o -> {
                txt.setValue(null);
                callback.valueChanged(null);
            });
            DateInputFormat mask = new DateInputFormat(between ? "##/##/####" + separator + "##/##/####" : "##/##/####", between, separator);
            mask.setCommitsOnValidEdit(true);
            mask.setPlaceholderCharacter('-');
            DefaultFormatterFactory df = new DefaultFormatterFactory(mask);
            txt.setFormatterFactory(df);

            PropertyChangeListener propertyChangeListener = evt -> callback.valueChanged(txt.getValue());
            txt.addPropertyChangeListener("value", propertyChangeListener);
            putPropertyChange(txt, propertyChangeListener);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void putPropertyChange(JFormattedTextField txt, PropertyChangeListener events) {
        if (inputMap == null) {
            inputMap = new HashMap<>();
        }
        inputMap.put(txt, events);
    }

    private static void removePropertyChange(JFormattedTextField txt) {
        if (inputMap == null) {
            return;
        }
        PropertyChangeListener event = inputMap.get(txt);
        if (event != null) {
            txt.removePropertyChangeListener("value", event);
        }
    }

    private static class TimeInputFormat extends MaskFormatter {

        private final boolean use24h;

        public TimeInputFormat(String mark, boolean use24h) throws ParseException {
            super(mark);
            this.use24h = use24h;
        }

        @Override
        public Object stringToValue(String value) throws ParseException {
            checkTime(value);
            return super.stringToValue(value);
        }

        public void checkTime(String value) throws ParseException {
            DateFormat df = new SimpleDateFormat(use24h ? "HH:mm" : "hh:mm aa");
            df.setLenient(false);
            df.parse(value);
        }
    }

    private static class DateInputFormat extends MaskFormatter {

        private final boolean between;
        private final String separator;

        public DateInputFormat(String mark, boolean between, String separator) throws ParseException {
            super(mark);
            this.between = between;
            this.separator = separator;
        }

        @Override
        public Object stringToValue(String value) throws ParseException {
            checkTime(value);
            return super.stringToValue(value);
        }

        public void checkTime(String value) throws ParseException {
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            df.setLenient(false);
            if (between) {
                String[] values = value.split(separator);
                df.parse(values[0]);
                df.parse(values[1]);
            } else {
                df.parse(value);
            }
        }
    }

    public interface ValueCallback {
        void valueChanged(Object value);
    }
}
