package boom.projectrm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Kim on 3/20/2016.
 */
public class FragmentTwo extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_two,container, false);
    }

    public void onClickUpload(View view) {
        Button uploadAct = (Button) view;
        Intent myIntent = new Intent(getActivity(), AddPhoto.class);
        myIntent.putExtra("boom.realmaps.EXTRA_CURR_LATITUDE", 10.10); // todo pass current latitude
        myIntent.putExtra("boom.realmaps.EXTRA_CURR_LONGITUDE", 10.10); // todo pass current longitude
        startActivity(myIntent);
    }

}
