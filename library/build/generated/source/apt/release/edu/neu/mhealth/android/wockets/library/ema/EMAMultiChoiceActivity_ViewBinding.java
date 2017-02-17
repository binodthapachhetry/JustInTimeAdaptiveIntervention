// Generated code from Butter Knife. Do not modify!
package edu.neu.mhealth.android.wockets.library.ema;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Finder;
import edu.neu.mhealth.android.wockets.library.R;
import java.lang.IllegalStateException;
import java.lang.Object;
import java.lang.Override;

public class EMAMultiChoiceActivity_ViewBinding<T extends EMAMultiChoiceActivity> implements Unbinder {
  protected T target;

  private View view2131624213;

  private View view2131624214;

  public EMAMultiChoiceActivity_ViewBinding(final T target, Finder finder, Object source) {
    this.target = target;

    View view;
    target.questionTextView = finder.findRequiredViewAsType(source, R.id.wockets_activity_ema_multi_choice_text_question, "field 'questionTextView'", TextView.class);
    target.linearLayout = finder.findRequiredViewAsType(source, R.id.wockets_activity_ema_multi_choice_checkbox_group, "field 'linearLayout'", LinearLayout.class);
    view = finder.findRequiredView(source, R.id.wockets_activity_ema_multi_choice_button_back, "field 'backButton' and method 'onClickBackButton'");
    target.backButton = finder.castView(view, R.id.wockets_activity_ema_multi_choice_button_back, "field 'backButton'", Button.class);
    view2131624213 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickBackButton();
      }
    });
    view = finder.findRequiredView(source, R.id.wockets_activity_ema_multi_choice_button_next, "field 'nextButton' and method 'onClickNextButton'");
    target.nextButton = finder.castView(view, R.id.wockets_activity_ema_multi_choice_button_next, "field 'nextButton'", Button.class);
    view2131624214 = view;
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
    target.linearLayout = null;
    target.backButton = null;
    target.nextButton = null;

    view2131624213.setOnClickListener(null);
    view2131624213 = null;
    view2131624214.setOnClickListener(null);
    view2131624214 = null;

    this.target = null;
  }
}
