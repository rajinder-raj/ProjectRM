package boom.projectrm;

import android.content.DialogInterface;
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
public class FragmentTwo extends Fragment implements View.OnClickListener {
    private View view;
    private Button upload_button;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        view = inflater.inflate(R.layout.fragment_two,container, false);
        upload_button = (Button) view.findViewById(R.id.uploadAct);
        upload_button.setOnClickListener(this);

        return view;
    }

    public void onClick(View v) {
        Button uploadAct = (Button) v;
        Intent myIntent = new Intent(getActivity(), AddPhoto.class);
        myIntent.putExtra("boom.realmaps.EXTRA_CURR_LATITUDE", 10.10); // todo pass current latitude
        myIntent.putExtra("boom.realmaps.EXTRA_CURR_LONGITUDE", 10.10); // todo pass current longitude
        startActivity(myIntent);
    }

}
