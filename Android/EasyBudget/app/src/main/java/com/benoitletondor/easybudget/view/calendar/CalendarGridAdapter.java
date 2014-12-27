package com.benoitletondor.easybudget.view.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.benoitletondor.easybudget.R;
import com.benoitletondor.easybudget.helper.ParameterKeys;
import com.benoitletondor.easybudget.helper.Parameters;
import com.benoitletondor.easybudget.model.db.DB;
import com.roomorama.caldroid.CaldroidGridAdapter;

import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

/**
 * @author Benoit LETONDOR
 */
public class CalendarGridAdapter extends CaldroidGridAdapter
{
    private DB db;
    private int baseBalance;

// ----------------------------------->

    public CalendarGridAdapter(Context context, int month, int year, HashMap<String, Object> caldroidData, HashMap<String, Object> extraData)
    {
        super(context, month, year, caldroidData, extraData);

        db = new DB(context.getApplicationContext());
        baseBalance = Parameters.getInstance(context).getInt(ParameterKeys.BASE_BALANCE, 0);
    }

    @Override
    protected void finalize() throws Throwable
    {
        db.close();

        super.finalize();
    }

// ----------------------------------->

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View cellView = convertView;
        ViewData viewData = null;

        // For reuse
        if (convertView == null)
        {
            cellView = createView(parent);
            viewData = new ViewData();

            viewData.dayTextView = (TextView) cellView.findViewById(R.id.grid_cell_tv1);
            viewData.amountTextView = (TextView) cellView.findViewById(R.id.grid_cell_tv2);
            viewData.cellColorIndicator = cellView.findViewById(R.id.cell_color_indicator);
        }
        else
        {
            viewData = (ViewData) cellView.getTag();
        }

        // Get dateTime of this cell
        DateTime dateTime = this.datetimeList.get(position);
        boolean isToday = dateTime.equals(getToday());

        TextView tv1 = viewData.dayTextView;
        TextView tv2 = viewData.amountTextView;
        View cellColorIndicator = viewData.cellColorIndicator;

        // Customize for disabled dates and date outside min/max dates
        if ((minDateTime != null && dateTime.lt(minDateTime))
                || (maxDateTime != null && dateTime.gt(maxDateTime))
                || (disableDates != null && disableDatesMap.containsKey(dateTime))
                || (dateTime.getMonth() != month) )
        {

            if( !viewData.isDisabled )
            {
                tv1.setTextColor(context.getResources().getColor(R.color.divider));
                tv2.setTextColor(context.getResources().getColor(R.color.divider));

                viewData.isDisabled = true;
            }
        }
        else if( viewData.isDisabled )
        {
            tv1.setTextColor(context.getResources().getColor(R.color.primary_text));
            tv2.setTextColor(context.getResources().getColor(R.color.secondary_text));

            viewData.isDisabled = false;
        }

        // Today's cell
        if( isToday )
        {
            // Customize for selected dates
            if (selectedDates != null && selectedDatesMap.containsKey(dateTime))
            {
                if( !viewData.isToday || !viewData.isSelected )
                {
                    cellView.setBackgroundResource(R.drawable.custom_grid_today_cell_selected_drawable);

                    viewData.isToday = true;
                    viewData.isSelected = true;
                }
            }
            else if( !viewData.isToday || viewData.isSelected )
            {
                cellView.setBackgroundResource(R.drawable.custom_grid_today_cell_drawable);

                viewData.isToday = true;
                viewData.isSelected = false;
            }
        }
        else
        {
            // Customize for selected dates
            if (selectedDates != null && selectedDatesMap.containsKey(dateTime))
            {
                if( viewData.isToday || !viewData.isSelected )
                {
                    cellView.setBackgroundResource(R.drawable.custom_grid_cell_selected_drawable);

                    viewData.isToday = false;
                    viewData.isSelected = true;
                }
            }
            else if( viewData.isToday || viewData.isSelected )
            {
                cellView.setBackgroundResource(R.drawable.custom_grid_cell_drawable);

                viewData.isToday = false;
                viewData.isSelected = false;
            }
        }

        tv1.setText("" + dateTime.getDay());

        Date date = new Date(dateTime.getMilliseconds(TimeZone.getTimeZone("UTC")));
        if( db.hasExpensesForDay(date) )
        {
            int balance = db.getBalanceForDay(date);

            if( !viewData.containsExpenses )
            {
                tv2.setVisibility(View.VISIBLE);
                cellColorIndicator.setVisibility(View.VISIBLE);

                viewData.containsExpenses = true;
            }

            tv2.setText((baseBalance-balance)+"");

            if( balance > 0 )
            {
                cellColorIndicator.setBackgroundResource(R.color.budget_red);
            }
            else if( balance <= 0 )
            {
                cellColorIndicator.setBackgroundResource(R.color.budget_green);
            }

            // Apply margin to the color indicator if it's today's cell since there's a border
            if( isToday && !viewData.colorIndicatorMarginForToday )
            {
                int marginDimen = context.getResources().getDimensionPixelOffset(R.dimen.grid_cell_today_border_size);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(cellColorIndicator.getLayoutParams());
                params.setMargins(0, marginDimen, marginDimen, 0);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                cellColorIndicator.setLayoutParams(params);

                viewData.colorIndicatorMarginForToday = true;
            }
            else if( !isToday && viewData.colorIndicatorMarginForToday )
            {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(cellColorIndicator.getLayoutParams());
                params.setMargins(0, 0, 0, 0);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                cellColorIndicator.setLayoutParams(params);

                viewData.colorIndicatorMarginForToday = false;
            }
        }
        else if( viewData.containsExpenses )
        {
            cellColorIndicator.setVisibility(View.GONE);
            tv2.setVisibility(View.INVISIBLE);

            viewData.containsExpenses = false;
        }

        cellView.setTag(viewData);
        return cellView;
    }

    /**
     * Inflate a new cell view
     *
     * @param parent
     * @return
     */
    private View createView(ViewGroup parent)
    {
        return LayoutInflater.from(context).inflate(R.layout.custom_grid_cell, parent, false);
    }

// --------------------------------------->

    /**
     * Object that represent data of a cell for optimization purpose
     */
    public static class ViewData
    {
        /**
         * TextView that contains the day
         */
        public TextView dayTextView;
        /**
         * TextView that contains the amount of money for the day
         */
        public TextView amountTextView;
        /**
         * View that display the color indicator of amount of money for the day
         */
        public View     cellColorIndicator;

        /**
         * Is this cell a disabled date
         */
        public boolean isDisabled                   = false;
        /**
         * Is this cell today's cell
         */
        public boolean isToday                      = false;
        /**
         * Is this cell selected
         */
        public boolean isSelected                   = false;
        /**
         * Does this cell contain expenses
         */
        public boolean containsExpenses             = false;
        /**
         * Are color indicator margin set for today
         */
        public boolean colorIndicatorMarginForToday = false;
    }
}
