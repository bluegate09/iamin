package idv.tfp10101.iamin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handleBottomNavigationView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        handleBottomNavigationView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleBottomNavigationView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handleBottomNavigationView();
    }

    @Override
    protected void onStop() {
        super.onStop();
        handleBottomNavigationView();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        handleBottomNavigationView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handleBottomNavigationView();
    }

    private void handleBottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (

                    navController.getCurrentDestination().getId() == R.id.homeFragment ||
                            navController.getCurrentDestination().getId() == R.id.chatFragment ||
                            navController.getCurrentDestination().getId() == R.id.logInFragment
            ) {
                bottomNavigationView.setVisibility(View.VISIBLE);
            } else {
                bottomNavigationView.setVisibility(View.GONE);
            }
        });
    }
}