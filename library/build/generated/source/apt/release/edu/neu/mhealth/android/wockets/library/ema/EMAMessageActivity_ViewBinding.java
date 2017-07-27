// Generated code from Butter Knife. Do not modify!
package edu.neu.mhealth.android.wockets.library.ema;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Finder;
import edu.neu.mhealth.android.wockets.library.R;
import java.lang.IllegalStateException;
import java.lang.Object;
import java.lang.Override;

public class EMAMessageActivity_ViewBinding<T extends EMAMessageActivity> implements Unbinder {
  protected T target;

  private View view2131624223;

  private View view2131624224;

  public EMAMessageActivity_ViewBinding(final T target, Finder finder, Object source) {
    this.target = target;

    View view;
    target.messageTextView = finder.findRequiredViewAsType(source, R.id.wockets_activity_ema_message_text_message, "field 'messageTextView'", TextView.class);
    view = finder.findRequiredView(source, R.id.wockets_activity_ema_message_button_back, "field 'backButton' and method 'onClickBackButton'");
    target.backButton = finder.castView(view, R.id.wockets_activity_ema_message_button_back, "field 'backButton'", Button.class);
    view2131624223 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickBackButton();
      }
    });
    view = finder.findRequiredView(source, R.id.wockets_activity_ema_message_button_next, "field 'nextButton' and method 'onClickNextButton'");
    target.nextButton = finder.castView(view, R.id.wockets_activity_ema_message_button_next, "field 'nextButton'", Button.class);
    view2131624224 = view;
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

    target.messageTextView = null;
    target.backButton = null;
    target.nextButton = null;

    view2131624223.setOnClickListener(null);
    view2131624223 = null;
    view2131624224.setOnClickListener(null);
    view2131624224 = null;

    this.target = null;
  }
}
