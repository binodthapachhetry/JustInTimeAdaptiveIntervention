// Generated code from Butter Knife. Do not modify!
package edu.neu.mhealth.android.wockets.library.activities;

import android.view.View;
import android.widget.EditText;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Finder;
import edu.neu.mhealth.android.wockets.library.R;
import java.lang.IllegalStateException;
import java.lang.Object;
import java.lang.Override;

public class LoginActivity_ViewBinding<T extends LoginActivity> implements Unbinder {
  protected T target;

  private View view2131624233;

  public LoginActivity_ViewBinding(final T target, Finder finder, Object source) {
    this.target = target;

    View view;
    target.email = finder.findRequiredViewAsType(source, R.id.wockets_activity_login_email, "field 'email'", EditText.class);
    target.password = finder.findRequiredViewAsType(source, R.id.wockets_activity_login_password, "field 'password'", EditText.class);
    view = finder.findRequiredView(source, R.id.wockets_activity_login_login, "method 'onClickLogin'");
    view2131624233 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickLogin(p0);
      }
    });
  }

  @Override
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.email = null;
    target.password = null;

    view2131624233.setOnClickListener(null);
    view2131624233 = null;

    this.target = null;
  }
}
