// Generated code from Butter Knife. Do not modify!
package edu.neu.mhealth.android.wockets.library.ema;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Finder;
import edu.neu.mhealth.android.wockets.library.R;
import java.lang.IllegalStateException;
import java.lang.Object;
import java.lang.Override;

public class EMATextInputActivity_ViewBinding<T extends EMATextInputActivity> implements Unbinder {
  protected T target;

  private View view2131624241;

  private View view2131624242;

  public EMATextInputActivity_ViewBinding(final T target, Finder finder, Object source) {
    this.target = target;

    View view;
    target.questionTextView = finder.findRequiredViewAsType(source, R.id.wockets_activity_ema_textinput_text_question, "field 'questionTextView'", TextView.class);
    target.textinput = finder.findRequiredViewAsType(source, R.id.wockets_activity_ema_textinput_textinput, "field 'textinput'", EditText.class);
    view = finder.findRequiredView(source, R.id.wockets_activity_ema_textinput_button_back, "field 'backButton' and method 'onClickBackButton'");
    target.backButton = finder.castView(view, R.id.wockets_activity_ema_textinput_button_back, "field 'backButton'", Button.class);
    view2131624241 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickBackButton();
      }
    });
    view = finder.findRequiredView(source, R.id.wockets_activity_ema_textinput_button_next, "field 'nextButton' and method 'onClickNextButton'");
    target.nextButton = finder.castView(view, R.id.wockets_activity_ema_textinput_button_next, "field 'nextButton'", Button.class);
    view2131624242 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickNextButton();
      }
    });
  }

  @Override
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.questionTextView = null;
    target.textinput = null;
    target.backButton = null;
    target.nextButton = null;

    view2131624241.setOnClickListener(null);
    view2131624241 = null;
    view2131624242.setOnClickListener(null);
    view2131624242 = null;

    this.target = null;
  }
}
