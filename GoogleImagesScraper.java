import java.util.*;
import java.io.*;
 // Webscraping
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;


class GoogleImagesScraper
{

// - Variables - //

    // Data
    String searchUrl;
    String searchHtml;
    // Max number of images can check (only so many loaded in page)
    int MaxNumImages = 25;
    // Scraper
    private String textAtImageResultLeft    = "data-ictx=\"1\" data-id=\"";
    private String textAtImageResultRight   = "\"";
    private int    textAtImageResultLeftLen = 23;
    private String textAtImageLeft    = "\",225,225]\n,[\"https://";
    private String textAtImageRight   = "\",";
    private int    textAtImageLeftLen = 14;


// - Main Methods - //


    // Set the search results for the google images search
    public void setSearch( String inSearchKeywords, int inNumTries )
    {

        try
        {

            // Get page url and html
            searchUrl = "https://www.google.com/search?tbm=isch&tbs=isz:lt,islt:vga,iar:s,ift:jpg&q=" + convertToSearchUrl( inSearchKeywords );
            searchHtml = Jsoup.connect( searchUrl ).get().html();
            // Cut down to title/href parts so less processing
            searchHtml = cutOutMainData();

        }
        catch ( IOException e )
        {

            // IF num of tries is > 0, try again, otherwise recurse back up
            if ( inNumTries > 0 )
            {
                setSearch( inSearchKeywords, inNumTries - 1 );
            }

        }

    }


    // Get the n'th image's url
    public String getImageUrl( int inImageNum ) throws IOException
    {

        String imageResultUrl = null;
        String outUrl = null;
        int curIndex = 0;
        int urlLeft;
        int urlRight;
        int i;

        // Go to n'th image url
        for ( i = 0; i < inImageNum; i++ )
        {
            curIndex = searchHtml.indexOf( textAtImageResultLeft, curIndex ) + 1;
        }

        // Cut out url
        curIndex--;
        urlLeft = searchHtml.indexOf( textAtImageResultLeft, curIndex ) + textAtImageResultLeftLen;
        urlRight = searchHtml.indexOf( textAtImageResultRight, urlLeft );
        imageResultUrl = searchUrl + "#imgrc=" + searchHtml.substring( urlLeft, urlRight );

        outUrl = getImageResultsImageUrl( imageResultUrl );

        return outUrl;

    }


// - Accessors - //


    // Get the page's html
    public String getHtml()
    {
        return searchHtml;
    }


// - Private Methods - //


    // Get a given image result's image's url
    private String getImageResultsImageUrl( String inResultUrl ) throws IOException
    {

        String outUrl = null;
        String imageResultHtml = null;
        int urlLeft;
        int urlRight;
        
        // Get image result's html
        imageResultHtml = Jsoup.connect( inResultUrl ).get().html();

        // Cut out image's url
        urlLeft = imageResultHtml.indexOf( textAtImageLeft ) + textAtImageLeftLen;
        urlRight = imageResultHtml.indexOf( textAtImageRight, urlLeft );
        outUrl = imageResultHtml.substring( urlLeft, urlRight );

        // Replace unsupported chars with actual chars
        outUrl = fixUnsupportedChars( outUrl );

        return outUrl;

    }


    // Get video titles and href's from html
    private String cutOutMainData() throws IOException
    {
        String outMainData = null;

        // IF html does not include required text, error in retrieving html originally, so throw exception
        if ( searchHtml.indexOf( textAtImageResultLeft ) < 0 )
        {
            throw new IOException();
        }

        // ELSE get main data and return
        outMainData = searchHtml.substring( searchHtml.indexOf( textAtImageResultLeft ), searchHtml.lastIndexOf( textAtImageResultLeft ) + 50 );

        return outMainData;
    }


    // Change certain chars in given string to make suitable for URL search
    private String convertToSearchUrl( String inSearch )
    {
        String outSearch = inSearch;
        
        outSearch = outSearch.replace( "&", "%26" );
        outSearch = outSearch.replace( ",", "%2C" );
        outSearch = outSearch.replace( "\\+", "%2B" );
        outSearch = outSearch.replace( ' ', '+' );
        
        return outSearch;
    }


    // Change unsupported chars in image url
    private String fixUnsupportedChars( String inString )
    {
        String outString = inString;

        outString = outString.replace( "\\u003d", "=" );
        outString = outString.replace( "\\u0026", "&" );
        
        return outString;
    }


}