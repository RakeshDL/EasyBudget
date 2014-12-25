package com.benoitletondor.easybudget.model;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Benoit LETONDOR
 */
public class OneTimeExpense extends Expense
{
    private Date date;

// ------------------------------------>

    public OneTimeExpense(int amount, Date date)
    {
        super(amount);

        if( date == null )
        {
            throw new NullPointerException("date==null");
        }

        this.date = cleanDate(date);
    }

// ------------------------------------>

    public Date getDate()
    {
        return date;
    }
}
