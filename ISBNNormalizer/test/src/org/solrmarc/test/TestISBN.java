

package org.solrmarc.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.solrmarc.mixin.ISBNNormalizer;


public class TestISBN
{
    
    static String testdata[][] = {
        { "0292735243" , "0292735243", "9780292735248" },
        { "0805086919 (hbk.)", "0805086919", "9780805086911" },
        { "9780805086911 (hbk.)", "0805086919", "9780805086911" },
        { "3319502425", "3319502425", "9783319502427"},
        { "092063303X (pbk.)", "092063303X", "9780920633038" },
        { "09-206-3303X (pbk.)", "092063303X", "9780920633038" },
        { "3129351108 (Klett-Cotta : Bd. 1)", "3129351108", "9783129351109" },
        { "978-0-521-30232-6 hardback", "0521302323", "9780521302326" },
        { "0000000000us-dc", "0000000000", "9780000000002" },
        { "285297150X (v. 1, pt. 1)", "285297150X", "9782852971509" },
        { "9782852971509 (v. 1, pt. 1)", "285297150X", "9782852971509" },
        { "1728837121736 (v. 1-2)", "invalid", "invalid" },
        { "0000000429T-4 (microfiche)", "invalid", "invalid" },
        { "0931-6418", "invalid", "invalid" },
        { "0849317304 (978-0-8493-1730-9 : alk. paper)", "0849317304", "9780849317309" },
        { "978013-2371568 (print)", "0132371561", "9780132371568" },
        { "978-1-4338-2092-2 (pbk. alk paper)", "1433820927", "9781433820922" },
        { "9773070093 (v. 4) :", "9773070093", "9789773070090" },
        { "979-1907094209", "No-10", "9791907094209" },
        { "9790051246373 (pbk.) :$", "No-10", "9790051246373" },
//        { "", "", "" },
//        { "", "", "" },
//        { "", "", "" },
//        { "", "", "" },
    };
    
    @Test
    public void testISBN10Match()
    {
        for (String[] entry : testdata)
        {
            try {
                String result = ISBNNormalizer.normalize_10(entry[0]);
                if (!entry[1].equals(result))
                    fail("ISBN 10 match failure");
            }
            catch (IllegalArgumentException iae)
            {
                if (! entry[1].equals("No-10") &&  !entry[1].equals("invalid"))
                {
                    fail("ISBN 10 match failure");
                }
            }
        }
        System.out.println("Test testISBN10Match is successful");        
    }
    
    @Test
    public void testISBN13Match()
    {
        for (String[] entry : testdata)
        {
            try {
                String result = ISBNNormalizer.normalize_13(entry[0]);
                if (!entry[2].equals(result))
                    fail("ISBN 13 match failure");
            }
            catch (IllegalArgumentException iae)
            {
                if (!entry[2].equals("invalid"))
                    fail("ISBN 13 match failure");
            }
        }
        System.out.println("Test testISBN13Match is successful");        
    }
    
    @Test
    public void testBothParm()
    {
        for (String[] entry : testdata)
        {
            Collection<String> expected = buildExpectedResult(entry);
            try {
                Collection<String> result = ISBNNormalizer.filterISBN(Collections.singletonList(entry[0]), "both");
                assertEquals(result.size(), expected.size());
                for (String res : result)
                {
                    assert(expected.contains(res));
                }
            }
            catch (IllegalArgumentException iae)
            {
                if (!entry[2].equals("invalid"))
                    fail("ISBN 13 match failure");
            }
        }
        System.out.println("Test testBothParm is successful");        

    }

    private Collection<String> buildExpectedResult(String[] entry)
    {
        List<String> result = new ArrayList<String>();
        if (! entry[1].equals("No-10") &&  !entry[1].equals("invalid")) 
            result.add(entry[1]);
        if (!entry[2].equals("invalid")) 
            result.add(entry[2]);
        return(result);

    }
}

