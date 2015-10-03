package app.sunshine.android.example.com.popmovies;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback {

    private boolean mTwoPane;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.indigo_500)));
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.fragment_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_detail_container, new DetailActivityFragment());
            } else {
                mTwoPane = false;
            }
        }
    }


    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.

            DetailActivityFragment fragment = new DetailActivityFragment();
            Bundle args = new Bundle();
            args.putString(Intent.EXTRA_TEXT, id);
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_detail_container, fragment)
                    .commit();
        } else {
            Intent detailIntent = new Intent(this, DetailActivity.class);
            detailIntent.setType("text/plain");
            detailIntent.putExtra(Intent.EXTRA_TEXT, id);
            startActivity(detailIntent);
        }
    }
}