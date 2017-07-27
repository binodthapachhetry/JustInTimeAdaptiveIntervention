// Generated code from Butter Knife. Do not modify!
package edu.neu.mhealth.android.wockets.library.ema;

import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Finder;
import edu.neu.mhealth.android.wockets.library.R;
import java.lang.IllegalStateException;
import java.lang.Object;
import java.lang.Override;

public class EMASingleChoiceActivity_ViewBinding<T extends EMASingleChoiceActivity> implements Unbinder {
  protected T target;

  private View view2131624235;

  private View view2131624236;

  public EMASingleChoiceActivity_ViewBinding(final T target, Finder finder, Object source) {
    this.target = target;

    View view;
    target.questionTextView = finder.findRequiredViewAsType(source, R.id.wockets_activity_ema_single_choice_text_question, "field 'questionTextView'", TextView.class);
    target.radioGroup = finder.findRequiredViewAsType(source, R.id.wockets_activity_ema_single_choice_radio_group, "field 'radioGroup'", RadioGroup.class);
    view = finder.findRequiredView(source, R.id.wockets_activity_ema_single_choice_button_back, "field 'backButton' and method 'onClickBackButton'");
    target.backButton = finder.castView(view, R.id.wockets_activity_ema_single_choice_button_back, "field 'backButton'", Button.class);
    view2131624235 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickBackButton();
      }
    });
    view = finder.findRequiredView(source, R.id.wockets_activity_ema_single_choice_button_next, "field 'nextButton' and method 'onClickNextButton'");
    target.nextButton = finder.castView(view, R.id.wockets_activity_ema_single_choice_button_next, "field 'nextButton'", Button.class);
    view2131624236 = view;
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
    target.radioGroup = null;
    target.backButton = null;
    target.nextButton = null;

    view2131624235.setOnClickListener(null);
    view2131624235 = null;
    view2131624236.setOnClickListener(null);
    view2131624236 = null;

    this.target = null;
  }
}
