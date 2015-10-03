package app.sunshine.android.example.com.popmovies;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;


public class DetailActivity extends AppCompatActivity {

    private String movieId;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.indigo_500)));
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.indigo_700));
        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            movieId = extras.getString(Intent.EXTRA_TEXT);
        }

        if(savedInstanceState == null){
            DetailActivityFragment fragment = new DetailActivityFragment();
            Bundle args = new Bundle();
            args.putString(Intent.EXTRA_TEXT, movieId);
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_detail_container,fragment).commit();
        }

        setContentView(R.layout.activity_detail);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
