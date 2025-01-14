package com.example.tipper.whatsfordinner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class EditDishActivity extends AppCompatActivity {

    private Context EditDishContext;
    private Activity EditDishActivity;
    private RelativeLayout EditDishLayout;
    private PopupWindow EditDishPopupWindow;
    private PopupWindow uploadImageURLPopupWindow;
    private ImageView addImage;

    public static final String EDIT_RECIPE_PREF = "EDIT_RECIPE_PREF" ;
    public static final String recipeKey = "recipeKey";
    private ArrayList<Ingredient> ingredients;
    private ArrayList<Ingredient> ingredientList;
    private ArrayAdapter<String> adapter;
    private ArrayAdapter<String> adapterSpinner;
    private AutoCompleteTextView actv;
    private Spinner spinnerUnit;
    private EditText editTextUnitNum;

    private DatabaseHandler db;
    private Recipe recipe;
    ArrayList<AutoCompleteTextView> AutoCompleteIngredientsList; //ingredients name list
    ArrayList<Spinner> unitList; // ingredient unit list
    ArrayList<EditText> unitNumList; // ingredient unit number list


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_dish);

        db = new DatabaseHandler(this);
        ingredientList = new ArrayList<Ingredient>();
        AutoCompleteIngredientsList = new ArrayList<AutoCompleteTextView>();
        unitList = new ArrayList<Spinner>();
        unitNumList = new ArrayList<EditText>();

        recipe = new Recipe();
        recipe = retrieveRecipe(db);
        ingredientList = recipe.getIngredients();
        final LinearLayout ingredients_LinearLayout = (LinearLayout) findViewById(R.id.ingredients_linearLayout);
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);

        /********** DISPLAY RECIPE INFORMATION  **********/

        TextView updateTextView = (TextView) findViewById(R.id.submit_textView);
        updateTextView.setText("Update Recipe");

        // SETTING RECIPE NAME
        EditText recipeNameEditText = (EditText) findViewById(R.id.recipe_editText);
        recipeNameEditText.setText(recipe.getName());
        // End SETTING RECIPE NAME

        ImageView recipeImageView = (ImageView) findViewById(R.id.setFoodImage);

        // SETTING RECIPE IMAGE
        if(URLUtil.isValidUrl(recipe.getImagePath())) {
            // Image from the Internet
            URL url = null;
            try {
                url = new URL(recipe.getImagePath());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Picasso.with(getApplicationContext()).load(recipe.getImagePath()).into(recipeImageView);
            urlConnection.disconnect(); //avoid any response leakage
        } else if (recipe.getImagePath().equals(String.valueOf(2130837601))){
            recipeImageView.setImageResource(R.drawable.meal_icon);
        } else {
            // Local Image
            GalleryImageAdapter galleryImageAdapter= new GalleryImageAdapter(this);
            int imageID = Integer.parseInt(recipe.getImagePath());
            recipeImageView.setImageResource(galleryImageAdapter.mImageIds[imageID]);

        }  // End SETTING RECIPE IMAGE

        // SETTING RECIPE DESCRIPTION
        TextView recipeDescriptionTextView = (TextView) findViewById(R.id.recipe_textArea);
        recipeDescriptionTextView.setText(recipe.getDescription());
        // End SETTING RECIPE DESCRIPTION

        for (int i = 0; i < ingredientList.size(); i++)
        {

            ArrayList<String> ingredientNameStrList = new ArrayList<String>();
            ingredientNameStrList.add(ingredientList.get(i).getIngredientName());

            //Creating the instance of ArrayAdapter containing list of fruit names
            adapter = new ArrayAdapter<String>
                    (getApplicationContext(), android.R.layout.select_dialog_item, ingredientNameStrList);
            adapterSpinner = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.units));

            actv = new AutoCompleteTextView(getApplicationContext()); //Ingredient Name
            editTextUnitNum = new EditText(getApplicationContext()); // Ingredient Unit Number
            spinnerUnit = new Spinner(getApplicationContext()); // Ingredient Unit --> lb, pieces, etc.

            actv.requestFocus();
            actv.setThreshold(1);//will start working from first character
            actv.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView

            spinnerUnit.setAdapter(adapterSpinner); //setting adapter data into EditSpinner (food units)
            spinnerUnit.setSelection(ingredientList.get(i).getIngredientUnitId());

            actv.setTextColor(Color.RED);
            actv.setHint("Enter an ingredient");
            actv.setLayoutParams(params);
            actv.setText(ingredientList.get(i).getIngredientName());

            editTextUnitNum.setHint("Enter unit number per measurement");
            editTextUnitNum.setInputType(InputType.TYPE_CLASS_NUMBER);
            editTextUnitNum.setText(String.valueOf(ingredientList.get(i).getIngredientCount()));

            ingredients_LinearLayout.addView(actv);
            ingredients_LinearLayout.addView(editTextUnitNum);
            ingredients_LinearLayout.addView(spinnerUnit);

            // ADD UNIT AND COUNT ON INGREDIENTS TABLE AND MANIPULATE DATA BASE ON THAT INFO
            AutoCompleteIngredientsList.add(actv);
            unitNumList.add(editTextUnitNum);
            unitList.add(spinnerUnit);

        }

        /********** UPLOAD IMAGE VIA URL OR LOCALLY POP UP WINDOW **********/
        uploadImagePopupWindow();

       /********** UPDATE RECIPE INFORMATION **********/

        //ADD INGREDIENTS
        TextView addIngredients = (TextView) findViewById(R.id.addIngredients_textView);
        addIngredients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ArrayList<String> ingredientNameStrList = new ArrayList<String>();
                ingredients = db.getAllIngredients();
                for (Ingredient i : ingredients) {
                    ingredientNameStrList.add(i.getIngredientName());
                }
                //Creating the instance of ArrayAdapter containing list of fruit names
                adapter = new ArrayAdapter<String>
                        (EditDishContext, android.R.layout.select_dialog_item, ingredientNameStrList);
                adapterSpinner = new ArrayAdapter<String>(EditDishContext, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.units));

                actv = new AutoCompleteTextView(EditDishContext); //Ingredient Name
                editTextUnitNum = new EditText(EditDishContext); // Ingredient Unit Number
                spinnerUnit = new Spinner(EditDishContext); // Ingredient Unit --> lb, pieces, etc.

                actv.requestFocus();
                actv.setThreshold(1);//will start working from first character
                actv.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView

                spinnerUnit.setAdapter(adapterSpinner); //setting adapter data into EditSpinner (food units)

                actv.setTextColor(Color.RED);
                actv.setHint("Enter an ingredient");
                actv.setLayoutParams(params);

                editTextUnitNum.setHint("Enter unit number per measurement");
                editTextUnitNum.setInputType(InputType.TYPE_CLASS_NUMBER);

                ingredients_LinearLayout.addView(actv);
                ingredients_LinearLayout.addView(editTextUnitNum);
                ingredients_LinearLayout.addView(spinnerUnit);

                // ADD UNIT AND COUNT ON INGREDIENTS TABLE AND MANIPULATE DATA BASE ON THAT INFO
                AutoCompleteIngredientsList.add(actv);
                unitNumList.add(editTextUnitNum);
                unitList.add(spinnerUnit);
            }
        });

        //SUBMIT INGREDIENTS
        TextView submitRecipe  = (TextView) findViewById(R.id.submit_textView);
        submitRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Ingredient> ingredientList = new ArrayList<Ingredient>();
                for(int i = 0; i < AutoCompleteIngredientsList.size()
                        && i < unitNumList.size()
                        && i < unitList.size(); i++) {

                    if(!AutoCompleteIngredientsList.get(i).getText().toString().isEmpty()
                            && !unitNumList.get(i).getText().toString().isEmpty()
                            && !unitList.get(i).getSelectedItem().toString().equals("Select unit of measurement")) {

                        Ingredient ingredient = new Ingredient();
                        ingredient.setIngredientName(AutoCompleteIngredientsList.get(i).getText().toString());
                        ingredient.setIngredientUnit(unitList.get(i).getSelectedItem().toString());
                        ingredient.setIngredientUnitId(unitList.get(i).getSelectedItemPosition());
                        ingredient.setIngredientCount(Integer.parseInt(unitNumList.get(i).getText().toString()));

                        ingredientList.add(ingredient);
                    }
                }



                EditText recipeName = (EditText) (findViewById(R.id.recipe_editText));
                EditText recipeDescription = (EditText) findViewById(R.id.recipe_textArea);

                if(!recipeName.getText().toString().isEmpty() && !recipeDescription.getText().toString().isEmpty() && !ingredientList.isEmpty()) {
                    recipe.setName(recipeName.getText().toString()); //store in Recipe
                    recipe.setDescription(recipeDescription.getText().toString());
                    recipe.setIngredients(ingredientList); //store in Recipe

                    /*

                    Add recipe to SQLite Database, Table: Recipes
                    where the recipes will be added to the SQLite Database
                    Set Default Image if user did not select image

                    */

                    int recipeAdded = 0;
                    if(recipe.getImagePath() == null) {
                        recipe.setImagePath(String.valueOf(R.drawable.meal_icon));
                        recipeAdded = db.updateRecipe(recipe);

                        if(recipeAdded == 1) { //if recipe updated properly and no errors have been thrown, show user success message
                            //Display Success
                            Toast toast = Toast.makeText(EditDishContext, "Updated recipe successfully", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP, 0, 0);
                            toast.show();

                            Intent intent = new Intent(getApplicationContext(), RecipeListActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        recipeAdded = db.updateRecipe(recipe);

                        if(recipeAdded == 1) {  //if recipe added properly and no errors have been thrown, show user success message
                            //Display Success
                            Toast toast = Toast.makeText(EditDishContext, "Updated recipe successfully", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP, 0, 0);
                            toast.show();

                            Intent intent = new Intent(getApplicationContext(), RecipeListActivity.class);
                            startActivity(intent);
                        }

                    }

                    //Adding new ingredients for Ingredient AutoCompleteTextView
                    for(int i = 0; i < ingredientList.size(); i++) {
                        db.addIngredient(EditDishContext, ingredientList.get(i).getIngredientName(), ingredientList.get(i).getIngredientUnit(), ingredientList.get(i).getIngredientCount());
                        adapter.notifyDataSetChanged();
                    }

                } else {
                    Toast toast = Toast.makeText(EditDishContext, "Empty recipe name, description and/or ingredients, please enter the recipe's name/description/ingredients", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                }

            }
        });




        db.close();

    }

    private Recipe retrieveRecipe(DatabaseHandler db)
    {
        SharedPreferences settings;
        String recipeName;
        SharedPreferences.Editor editor;
        Recipe recipe = new Recipe();
        settings = getApplicationContext().getSharedPreferences(EDIT_RECIPE_PREF, Context.MODE_PRIVATE);
        recipeName = settings.getString(recipeKey, null);
        if (recipeName != null) {
            recipe = db.getRecipe(recipeName);

        } //else set default image
        editor = settings.edit();
        editor.clear();
        editor.commit();

        return recipe;
    }

    private void uploadImagePopupWindow()
    {
        EditDishContext = getApplicationContext();
        EditDishActivity = EditDishActivity.this;

        EditDishLayout = (RelativeLayout) findViewById(R.id.rl_newdish_layout);
        addImage = (ImageView) findViewById(R.id.uploadImageView);

        //POPUP WINDOW CONFIGS: Asks the user to upload the image locally or via URL
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Initialize a new instance of LayoutInflater service
                LayoutInflater inflater = (LayoutInflater) EditDishContext.getSystemService(LAYOUT_INFLATER_SERVICE);

                // Inflate the custom layout/view
                View customView = inflater.inflate(R.layout.image_upload_popup_layout,null);

                /*
                    public PopupWindow (View contentView, int width, int height)
                        Create a new non focusable popup window which can display the contentView.
                        The dimension of the window must be passed to this constructor.

                        The popup does not provide any background. This should be handled by
                        the content view.

                    Parameters
                        contentView : the popup's content
                        width : the popup's width
                        height : the popup's height
                */
                // Initialize a new instance of popup window
                EditDishPopupWindow = new PopupWindow(
                        customView,
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        true
                );

                // Set an elevation value for popup window
                // Call requires API level 21
                if(Build.VERSION.SDK_INT>=21){
                   EditDishPopupWindow.setElevation(5.0f);
                }

                //UPLOAD IMAGE VIA URL
                Button uploadImageURL = (Button) customView.findViewById(R.id.upload_image_url_button);
                uploadImageURL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        EditDishPopupWindow.dismiss();

                        //Display popup window to enter image url manually
                        imageUrlPopUpWindow();
                    }
                });

                Button uploadImageLocally = (Button) customView.findViewById(R.id.upload_image_local_button);
                uploadImageLocally.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), GalleryActivity.class);
                        startActivity(intent);
                    }
                });

                // Get a reference for the custom view close button
                ImageButton closeButton = (ImageButton) customView.findViewById(R.id.ib_close);

                // Set a click listener for the popup window close button
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Dismiss the popup window
                        EditDishPopupWindow.dismiss();
                    }
                });

                /*
                    public void showAtLocation (View parent, int gravity, int x, int y)
                        Display the content view in a popup window at the specified location. If the
                        popup window cannot fit on screen, it will be clipped.
                        Learn WindowManager.LayoutParams for more information on how gravity and the x
                        and y parameters are related. Specifying a gravity of NO_GRAVITY is similar
                        to specifying Gravity.LEFT | Gravity.TOP.

                    Parameters
                        parent : a parent view to get the getWindowToken() token from
                        gravity : the gravity which controls the placement of the popup window
                        x : the popup's x location offset
                        y : the popup's y location offset
                */
                // Finally, show the popup window at the center location of root relative layout
                EditDishPopupWindow.showAtLocation(EditDishLayout, Gravity.CENTER,0,0);
            }
        });
    }
    private void imageUrlPopUpWindow()
    {
        // Initialize a new instance of LayoutInflater service
        LayoutInflater inflater = (LayoutInflater) EditDishContext.getSystemService(LAYOUT_INFLATER_SERVICE);

        // Inflate the custom layout/view
        final View imageUrlView = inflater.inflate(R.layout.image_upload_url_popup_layout,null);

        uploadImageURLPopupWindow = new PopupWindow(
                imageUrlView,
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        // Set an elevation value for popup window
        // Call requires API level 21
        if(Build.VERSION.SDK_INT>=21){
            uploadImageURLPopupWindow.setElevation(5.0f);
        }

        //SUBMITTING IMAGE URL
        Button submitButton = (Button) imageUrlView.findViewById(R.id.submitURL_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView uploadedImage = (ImageView) EditDishActivity.findViewById(R.id.setFoodImage);
                EditText imageUrl = (EditText) imageUrlView.findViewById(R.id.imageURL_editText);

                if (URLUtil.isValidUrl(imageUrl.getText().toString())) {
                    String validImageUrl = imageUrl.getText().toString().replaceAll("\\s+","");
                    try {
                        recipe.setImagePath(validImageUrl); //store in Recipe
                        URL url = new URL(validImageUrl);
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        Picasso.with(EditDishContext).load(validImageUrl).into(uploadedImage);
                        urlConnection.disconnect(); //avoid any response leakage
                        uploadImageURLPopupWindow.dismiss();
                    } catch(java.io.IOException e) {
                        Toast toast = Toast.makeText(EditDishContext, "Invalid URL, please try again", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 0);
                        toast.show();
                    }
                } else {
                    Toast toast = Toast.makeText(EditDishContext, "Invalid URL, please try again", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                }
            }
        });

        // Get a reference for the custom view close button
        ImageButton closeButton = (ImageButton) imageUrlView.findViewById(R.id.ib_close);

        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                uploadImageURLPopupWindow.dismiss();
            }
        });

        uploadImageURLPopupWindow.showAtLocation(EditDishLayout, Gravity.CENTER,0,0);

    }
}
