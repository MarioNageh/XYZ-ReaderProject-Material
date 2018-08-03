package com.example.xyzreader.ui.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.ui.ImageLoaderHelper;
import com.example.xyzreader.ui.activities.ArticleDetailActivity;
import com.example.xyzreader.ui.activities.ArticleListActivity;
import com.example.xyzreader.ui.customViews.DrawInsetsFrameLayout;
import com.example.xyzreader.ui.customViews.ObservableScrollView;
import com.squareup.picasso.Picasso;


public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    //For Log
    private static final String TAG = "ArticleDetailFragment";


    //Constant Values
    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_ITEM_TITLE = "item_Title";
    public static final String FONT_Rosario = "Rosario-Regular.ttf";

    // Cursor Loader
    private Cursor mCursor;
    //data Getter
    private long mItemId;
    private String mItemTitle;
    // Main View
    private View mRootView;
    //View
    private TextView title_txt,
            date_txt, text_article_body;
    private ImageView main_photo;
    private Toolbar toolbar_con;
    private CollapsingToolbarLayout Collapsing_layout;
    String titleString;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(String itemtitel) {

        Bundle arguments = new Bundle();
        arguments.putString(ARG_ITEM_TITLE, itemtitel);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }
        if (getArguments() != null && getArguments().containsKey(ARG_ITEM_TITLE)) {
            mItemTitle = getArguments().getString(ARG_ITEM_TITLE);
        }

    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        //this for Floation Share Todo Done
        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        bindViews();
        return mRootView;
    }


    static float progress(float v, float min, float max) {
        return constrain((v - min) / (max - min), 0, 1);
    }

    static float constrain(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }
        //TODO Bind All Views Here
        title_txt = (TextView) mRootView.findViewById(R.id.title_txt);
        date_txt = (TextView) mRootView.findViewById(R.id.date_txt);
        text_article_body = (TextView) mRootView.findViewById(R.id.text_article_body);
        main_photo = (ImageView) mRootView.findViewById(R.id.main_photo);
        Collapsing_layout = (CollapsingToolbarLayout) mRootView.findViewById(R.id.Collapsing_layout);
        toolbar_con = (Toolbar) mRootView.findViewById(R.id.toolbar_con);
        ///////////////////// Setup Fonts ///////////
        title_txt.setTypeface(Typeface.createFromAsset(getResources().getAssets(),
                FONT_Rosario));
        date_txt.setTypeface(Typeface.createFromAsset(getResources().getAssets(),
                FONT_Rosario));
        text_article_body.setTypeface(Typeface.createFromAsset(getResources().getAssets(),
                FONT_Rosario));

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().setDuration(300).alpha(1);

            // Setup The title Get from Curose
            titleString = mCursor.getString(ArticleLoader.Query.TITLE);
            Collapsing_layout.setTitle(titleString);
            toolbar_con.setTitle(titleString);
            toolbar_con.setTitleTextColor(Color.WHITE);
            ////// Setup Date
            Date publishedDate = parsePublishedDate();
            if (!publishedDate.before(START_OF_EPOCH.getTime())) {
                date_txt.setText(Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + " by <font color='#ffffff'>"
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>"));

            } else {
                // If date is before 1902, just show the string
                date_txt.setText(Html.fromHtml(
                        outputFormat.format(publishedDate) + " by <font color='#ffffff'>"
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>"));

            }
            //Fill Texts Views
            date_txt.setText(date_txt.getText().toString());
            date_txt.setTextColor(ContextCompat.getColor(getActivity().getBaseContext(), R.color.textcolor));
            title_txt.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY).replaceAll("(\r\n|\n)", "<br />")));
            text_article_body.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY).replaceAll("(\r\n|\n)", "<br />")));


             // Prepare Image By Picasso
            String photoUrl = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
            Picasso.with(getContext())
                    .load(photoUrl)
                    .into(main_photo);

        } else {
            mRootView.setVisibility(View.GONE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (mItemTitle != null && !mItemTitle.isEmpty()) {
            return ArticleLoader.newInstanceForItemId(getActivity(), mItemTitle);
        } else {

            return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }


}
