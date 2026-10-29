package org.familycheck.senior;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import java.util.HashMap;
public class DetailActivity extends BaseActivity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setup("Review your check");
        long id=getIntent().getLongExtra("id",-1);
        HashMap<String,String> row=db.find(id);
        if(row==null) {
            Ui.toast(this,"This check no longer exists");
            finish();
            return;
        }
        Ui.text(content,row.get("source")+" · "+android.text.format.DateFormat.format("dd MMM yyyy",Long.parseLong(row.get("created"))),14,false);
        LinearLayout card=Ui.card(content);
        Ui.text(card,row.get("message"),20,true);
        Ui.text(content,"Warning signs you selected",17,true);
        Ui.text(content,row.get("flags").isEmpty()?"None selected. This does not mean the message is genuine.":row.get("flags"),17,false);
        Ui.button(content,"Ask someone you trust",true,()-> {
            EditText preview=new EditText(this);
            preview.setText("Please help me check this before I act:\n\n"+row.get("message")+"\n\nMy concerns:\n"+(row.get("flags").isEmpty()?"I am unsure.":row.get("flags")));
            preview.setMinLines(4);
            preview.setMaxLines(9);
            preview.setTextSize(Ui.size(this,17));
            new AlertDialog.Builder(this).setTitle("Review before sharing").setMessage("Remove any passwords, OTPs or account details. You choose the recipient in the next app.").setView(preview).setNegativeButton("Cancel",null).setPositiveButton("Choose app",(d,w)-> {
                if(!preview.getText().toString().trim().isEmpty())Ui.share(this,preview.getText().toString());
            }
            ).show();
        }
        );
        Ui.button(content,"Call a trusted person",false,()->startActivity(new Intent(this,MainActivity.class).putExtra("tab",2)));
        Ui.text(content,"Record your assessment",20,true);
        Spinner status=new Spinner(this);
        status.setId(201);
        String[] choices= {
            "Still unsure","Reported as suspicious","Verified independently"
        }
        ;
        status.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,choices));
        status.setMinimumHeight(Ui.dp(this,48));
        content.addView(status);
        for(int n=0;n<choices.length;n++)if(choices[n].equals(row.get("status")))status.setSelection(n);
        EditText note=input("How did you check it?",202,2000);
        note.setText(row.get("note"));
        Ui.text(content,"Verify through a known contact or independently found official channel. This status is your assessment.",15,false);
        Ui.button(content,"Save outcome",true,()-> {
            String n=note.getText().toString().trim();
            if(status.getSelectedItemPosition()==2&&n.isEmpty()) {
                note.setError("Describe how you independently verified it");
                return;
            }
            db.outcome(id,status.getSelectedItem().toString(),n);
            Ui.toast(this,"Outcome saved");
            finish();
        }
        );
        Ui.button(content,"Set a follow-up reminder",false,()->startActivity(new Intent(this,ReminderActivity.class)));
        Ui.button(content,"Delete check",false,()->new AlertDialog.Builder(this).setTitle("Delete this check?").setMessage("This cannot be undone.").setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)-> {
            db.delete("cases",id);
            finish();
        }
        ).show());
    }
}
