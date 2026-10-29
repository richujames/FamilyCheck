package org.familycheck.senior;
import android.app.*;
import android.os.Bundle;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.*;
import java.util.HashMap;
public class ContactDialog extends DialogFragment {
    private EditText name,phone,relation;
    private CheckBox favorite;
    public static ContactDialog edit(HashMap<String,String> row) {
        ContactDialog d=new ContactDialog();
        Bundle b=new Bundle();
        for(String key:row.keySet())b.putString(key,row.get(key));
        d.setArguments(b);
        return d;
    }
    @Override public Dialog onCreateDialog(Bundle state) {
        Bundle values=state!=null?state:getArguments();
        LinearLayout form=Ui.column(getActivity());
        int p=Ui.dp(getActivity(),20);
        form.setPadding(p,p,p,p);
        name=field(form,"Name",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_WORDS,80);
        phone=field(form,"Phone number with country code",InputType.TYPE_CLASS_PHONE,30);
        relation=field(form,"Relationship (optional)",InputType.TYPE_CLASS_TEXT,40);
        favorite=new CheckBox(getActivity());
        favorite.setText("Favourite contact");
        favorite.setTextSize(Ui.size(getActivity(),17));
        form.addView(favorite);
        if(values!=null) {
            name.setText(values.getString("name",""));
            phone.setText(values.getString("phone",""));
            relation.setText(values.getString("relationship",""));
            favorite.setChecked("1".equals(values.getString("favorite")));
        }
        ScrollView scroll=new ScrollView(getActivity());
        scroll.addView(form);
        return new AlertDialog.Builder(getActivity()).setTitle(getArguments()==null?"Add trusted person":"Edit trusted person").setView(scroll).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();
    }
    private EditText field(LinearLayout parent,String label,int type,int max) {
        Ui.text(parent,label,15,true);
        EditText e=new EditText(getActivity());
        e.setHint(label);
        e.setTextSize(Ui.size(getActivity(),18));
        e.setInputType(type);
        e.setFilters(new android.text.InputFilter[] {
            new android.text.InputFilter.LengthFilter(max)
        }
        );
        parent.addView(e);
        return e;
    }
    @Override public void onStart() {
        super.onStart();
        ((AlertDialog)getDialog()).getButton(-1).setOnClickListener(v-> {
            String n=name.getText().toString().trim(),p=phone.getText().toString().replaceAll("[\\s()-]","");
            if(n.isEmpty()) {
                name.setError("Enter a name");
                return;
            }
            if(!p.matches("\\+?[0-9]{6,15}")) {
                phone.setError("Use 6–15 digits, optionally starting with +");
                return;
            }
            try(Db db=new Db(getActivity())) {
                db.contact(getArguments()==null?-1:Long.parseLong(getArguments().getString("_id")),n,p,relation.getText().toString().trim(),favorite.isChecked());
            }
            Ui.toast(getActivity(),"Trusted person saved");
            ((MainActivity)getActivity()).refresh();
            dismiss();
        }
        );
    }
    @Override public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        b.putString("name",name.getText().toString());
        b.putString("phone",phone.getText().toString());
        b.putString("relationship",relation.getText().toString());
        b.putString("favorite",favorite.isChecked()?"1":"0");
    }
}
