package boom.projectrm;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Kim on 3/20/2016.
 */
public class PagerAdapter extends FragmentPagerAdapter {

    public PagerAdapter(FragmentManager fm){
        super(fm);
    }
    @Override
    public Fragment getItem(int position){
        switch(position){
            case 0:
                return new MapsFragment();

            case 1:
                return new FragmentTwo();

            //case 2:
                //return new FragmentThree();

            default:
                return null;

        }

    }

    @Override
    public int getCount(){
        return 2;
    }
}
