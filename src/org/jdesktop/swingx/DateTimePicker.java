package org.jdesktop.swingx;

import org.jdesktop.swingx.calendar.SingleDaySelectionModel;

import java.text.DateFormat;
import java.util.Date;

/**
 * This is licensed under LGPL.  License can be found here:  http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * This is provided as is.  If you have questions please direct them to charlie.hubbard at gmail dot you know what.
 */
public class DateTimePicker extends JXDatePicker {
    private DateFormat timeFormat;

    public DateTimePicker() {
        super();
        getMonthView().setSelectionModel(new SingleDaySelectionModel());
    }

    public DateTimePicker( Date d ) {
        this();
        setDate(d);
    }

    public DateFormat getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(DateFormat timeFormat) {
        this.timeFormat = timeFormat;
    }
}
