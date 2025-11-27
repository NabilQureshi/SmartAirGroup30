package com.example.smartair.ui.parent;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.smartair.R;
import com.example.smartair.data.ChildRepository;
import com.example.smartair.model.Child;

/**
 * Dialog for setting or updating a child's Personal Best (PB) value.
 * Allows parents to input a PB value which is used for zone calculations.
 */
public class SetPersonalBestDialog extends DialogFragment {
    private static final String ARG_CHILD = "child";
    
    private Child child;
    private ChildRepository childRepository;
    private EditText pbInput;

    public static SetPersonalBestDialog newInstance(Child child) {
        SetPersonalBestDialog dialog = new SetPersonalBestDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CHILD, child);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            child = (Child) getArguments().getSerializable(ARG_CHILD);
        }

        if (child == null) {
            return super.onCreateDialog(savedInstanceState);
        }

        childRepository = new ChildRepository(requireContext());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_set_personal_best, null);

        TextView childNameText = view.findViewById(R.id.child_name_text);
        pbInput = view.findViewById(R.id.pb_input);
        TextView currentPbText = view.findViewById(R.id.current_pb_text);

        childNameText.setText("Set Personal Best for " + child.getName());

        if (child.hasPersonalBest()) {
            currentPbText.setText("Current PB: " + child.getPersonalBest() + " L/min");
            pbInput.setText(String.valueOf(child.getPersonalBest()));
        } else {
            currentPbText.setText("No Personal Best set");
        }

        pbInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        pbInput.setHint("Enter PB value (L/min)");

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle("Set Personal Best")
                .setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    savePersonalBest();
                })
                .setNegativeButton("Cancel", null);

        return builder.create();
    }

    private void savePersonalBest() {
        String pbString = pbInput.getText().toString().trim();
        
        if (pbString.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a Personal Best value", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int pbValue = Integer.parseInt(pbString);
            
            if (pbValue <= 0) {
                Toast.makeText(requireContext(), "Personal Best must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pbValue > 1000) {
                Toast.makeText(requireContext(), "Please enter a realistic value (typically 100-800 L/min)", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update the child's Personal Best
            childRepository.updatePersonalBest(child.getId(), pbValue);
            
            Toast.makeText(requireContext(), 
                "Personal Best updated to " + pbValue + " L/min", 
                Toast.LENGTH_SHORT).show();

            // Notify parent activity to refresh
            if (getActivity() instanceof ParentHomeActivity) {
                ((ParentHomeActivity) getActivity()).onResume();
            }

            dismiss();
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }
}

