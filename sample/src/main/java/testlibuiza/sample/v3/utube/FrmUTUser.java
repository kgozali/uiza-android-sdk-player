package testlibuiza.sample.v3.utube;

/**
 * Created by www.muathu@gmail.com on 9/1/2019.
 */

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import testlibuiza.R;
import uizacoresdk.interfaces.IOnBackPressed;

public class FrmUTUser extends Fragment implements IOnBackPressed {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.v4_frm_user, container, false);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
