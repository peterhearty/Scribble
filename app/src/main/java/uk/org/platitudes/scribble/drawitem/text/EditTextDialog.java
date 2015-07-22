package uk.org.platitudes.scribble.drawitem.text;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import uk.org.platitudes.scribble.R;
import uk.org.platitudes.scribble.drawitem.text.TextItem;

/**
 * See Android/Sdk/docs/guide/topics/ui/dialogs.html
 */
public class EditTextDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private EditText editText;
    private TextItem textItem;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.edit_text_dialog, null);
        editText = (EditText) v.findViewById(R.id.edit_text_box);
        editText.setText(textItem.getmText());

        builder.setView(v);
//        builder.setMessage("fire missiles?");

        builder.setPositiveButton("save", this);
        builder.setNegativeButton("cancel", this);
        // Create the AlertDialog object and return it
        return builder.create();
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            String textValue = editText.getText().toString();
            textItem.setmText(textValue);
        } else {
            // leave empty
        }

    }


    public void setTextItem(TextItem textItem) {this.textItem = textItem;}

}
