package org.familycheck.senior;
import android.os.Bundle;
import android.widget.*;
/** Small offline practice exercise. No score is used to judge real messages. */
public class QuizActivity extends BaseActivity {
    private int question,score,answer=-1;
    private final String[] questions= {
        "A caller says your account will close in ten minutes unless you share an OTP. What should you do?","A message from a familiar photo asks for urgent money. What is the best next step?","Someone says you must keep their payment request secret. What should you do?","A stranger asks you to install an app so they can fix your phone. What should you do?","A message has none of the warning signs in FamilyCheck. What does that mean?"
    }
    ;
    private final String[][] options= {
        {
            "Share the OTP quickly","End the call and use a known official contact","Ask them to send another OTP"
        }
        , {
            "Pay because the photo is familiar","Reply with your bank details","Call the person on a number you already know"
        }
        , {
            "Keep it secret","Pause and ask someone you trust","Delete all evidence immediately"
        }
        , {
            "Pause and independently check who they are","Install it to find out","Share your screen first"
        }
        , {
            "It is definitely genuine","It still needs independent verification","It is definitely a scam"
        }
    }
    ;
    private final int[] correct= {
        1,2,1,0,1
    }
    ;
    private final String[] why= {
        "Never share an OTP with a caller. Contact the organisation independently.","A familiar photo does not establish identity. Use a known number.","Secrecy can isolate you from useful advice. Get another opinion.","Do not install software on a stranger's instructions. Verify the request first.","A checklist cannot establish genuineness. Verify the claim independently."
    }
    ;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setup("A little practice");
        if(state!=null) {
            question=state.getInt("question");
            score=state.getInt("score");
            answer=state.getInt("answer",-1);
        }
        render();
    }
    private void render() {
        content.removeAllViews();
        if(question==questions.length) {
            Ui.text(content,"Practice complete",28,true);
            Ui.text(content,score+" of 5 correct",24,true);
            Ui.text(content,"Keep practising the habit: pause, check and ask. This score does not predict whether you will recognise a real scam.",18,false);
            Ui.button(content,"Try again",true,()-> {
                question=0;
                score=0;
                answer=-1;
                render();
            }
            );
            Ui.button(content,"Back to FamilyCheck",false,this::finish);
            return;
        }
        Ui.text(content,"QUESTION "+(question+1)+" OF 5",13,true);
        Ui.text(content,questions[question],23,true);
        for(int n=0;n<3;n++) {
            final int selected=n;
            Button b=Ui.button(content,options[question][n],false,()-> {
                answer=selected;
                if(answer==correct[question])score++;
                render();
            }
            );
            b.setEnabled(answer==-1);
        }
        if(answer!=-1) {
            Ui.text(content,answer==correct[question]?"That's the safer next step.":"Let's look at that again.",20,true);
            Ui.text(content,why[question],18,false);
            Ui.button(content,question==4?"See result":"Next question",true,()-> {
                question++;
                answer=-1;
                render();
            }
            );
        }
    }
    @Override protected void onSaveInstanceState(Bundle b) {
        b.putInt("question",question);
        b.putInt("score",score);
        b.putInt("answer",answer);
        super.onSaveInstanceState(b);
    }
}
