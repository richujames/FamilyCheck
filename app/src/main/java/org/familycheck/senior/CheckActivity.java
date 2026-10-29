package org.familycheck.senior;
import android.os.Bundle;
import android.content.Intent;
import android.widget.*;
import java.util.*;
public class CheckActivity extends BaseActivity {
    private final ArrayList<CheckBox> flags=new ArrayList<>();
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setup("Check a concern");
        Ui.text(content,"Take it one step at a time",26,true);
        Ui.text(content,"Describe what happened. Leave out passwords, OTPs and account details.",17,false);
        Ui.text(content,"1. Where did it happen?",17,true);
        Spinner source=new Spinner(this);
        source.setId(101);
        source.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[] {
            "Text message","Phone call","WhatsApp / social media","Email","Other"
        }
        ));
        source.setMinimumHeight(Ui.dp(this,48));
        content.addView(source);
        EditText message=input("2. Message or call details",102,5000);
        message.setMinLines(3);
        if(state==null&&Intent.ACTION_SEND.equals(getIntent().getAction())) {
            CharSequence shared=getIntent().getCharSequenceExtra(Intent.EXTRA_TEXT);
            if(shared!=null)message.setText(shared.subSequence(0,Math.min(5000,shared.length())));
        }
        Ui.text(content,"3. What made you unsure?",17,true);
        String[] labels= {
            "Asked for money, a password or an OTP","Pressured or threatened me to act quickly","Told me to keep it secret","Asked me to open a link or install an app","Unexpectedly claimed to be family, a bank or an official"
        }
        ;
        for(int n=0;n<labels.length;n++) {
            CheckBox c=new CheckBox(this);
            c.setId(110+n);
            c.setText(labels[n]);
            c.setTextSize(Ui.size(this,17));
            c.setMinHeight(Ui.dp(this,56));
            content.addView(c);
            flags.add(c);
        }
        Ui.text(content,"You can save a check even if none apply. This does not mean the message is genuine.",15,false);
        Ui.button(content,"Save & review",true,()-> {
            String msg=message.getText().toString().trim();
            if(msg.isEmpty()) {
                message.setError("Describe the concern first");
                return;
            }
            StringBuilder selected=new StringBuilder();
            for(CheckBox c:flags)if(c.isChecked())selected.append("• ").append(c.getText()).append('\n');
            long id=db.save(msg,selected.toString(),source.getSelectedItem().toString());
            startActivity(new Intent(this,DetailActivity.class).putExtra("id",id));
            finish();
        }
        );
    }
}
