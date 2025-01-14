package com.example.tipper.whatsfordinner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.util.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Created by tipper on 9/8/17.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "RecipeDB";

    // Recipe table name
    private static final String TABLE_RECIPES = "Recipe";

    // Recipe Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_INGREDIENTS = "ingredients";
    private static final String KEY_IMAGE_PATH = "image_path";
    private static final String KEY_DESCRIPTION = "description";

    // Ingredient table name
    private static final String TABLE_INGREDIENTS = "Ingredient";

    // Ingredient Table Column names
    private static final String KEY_INGREDIENTS_NAME = "ingredients";
    private static final String KEY_UNITS = "units";
    private static final String KEY_COUNT = "count";

    //Meal table name
    private static final String TABLE_MEALS = "Meal";

    //Meal Table Column names
    private static final String KEY_MEAL_NAME = "name";
    private static final String KEY_MEAL_COUNT = "count";

    // Meal Plan Table Name
    private static final String TABLE_MEAL_PLANS = "Meal_Plans";

    //Meal Plan Table Column names
    private static final String KEY_MEAL_PLAN_DATE = "date";
    private static final String KEY_MEAL_PLAN_BREAKFAST = "breakfast";
    private static final String KEY_MEAL_PLAN_LUNCH = "lunch";
    private static final String KEY_MEAL_PLAN_DINNER = "dinner";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v("Creating Table", "True");
        String CREATE_INGREDIENT_TABLE = "CREATE TABLE " + TABLE_INGREDIENTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_INGREDIENTS_NAME + " TEXT,"
                + KEY_UNITS + " TEXT,"
                + KEY_COUNT + " INTEGER,"
                +  "UNIQUE (" + KEY_INGREDIENTS_NAME + ") ON CONFLICT ROLLBACK)"; //helps avoid duplicates

        String CREATE_MEAL_TABLE = "CREATE TABLE " + TABLE_MEALS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_MEAL_NAME + " TEXT,"
                + KEY_MEAL_COUNT + " INTEGER "
                + ")";

        String CREATE_MEAL_PLAN_TABLE = "CREATE TABLE " + TABLE_MEAL_PLANS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_MEAL_PLAN_DATE + " TEXT,"
                + KEY_MEAL_PLAN_BREAKFAST + " TEXT, "
                + KEY_MEAL_PLAN_LUNCH + " TEXT, "
                + KEY_MEAL_PLAN_DINNER + " TEXT "
                + ")";

        String CREATE_RECIPE_TABLE = "CREATE TABLE " + TABLE_RECIPES + "("
                        + KEY_ID + " INTEGER PRIMARY KEY,"
                        + KEY_NAME + " TEXT,"
                        + KEY_INGREDIENTS + " TEXT, "
                        + KEY_IMAGE_PATH + " TEXT, "
                        + KEY_DESCRIPTION + " TEXT, "
                        +  "UNIQUE (" + KEY_NAME  + ") ON CONFLICT ROLLBACK )"; //helps avoid duplicates

        db.execSQL(CREATE_INGREDIENT_TABLE);
        db.execSQL(CREATE_MEAL_TABLE);
        db.execSQL(CREATE_MEAL_PLAN_TABLE);
        db.execSQL(CREATE_RECIPE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v("Upgrading Table", "True");
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);

        // Create tables again
        onCreate(db);
    }

    /********** MEAL PLAN OPERATIONS **********/
    public void addMealPlan(MealPlan mealPlan)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MEAL_PLAN_DATE, mealPlan.getDate());
        values.put(KEY_MEAL_PLAN_BREAKFAST, mealPlan.getBreakfast());
        values.put(KEY_MEAL_PLAN_LUNCH, mealPlan.getLunch());
        values.put(KEY_MEAL_PLAN_DINNER, mealPlan.getDinner());

        db.insert(TABLE_MEAL_PLANS, null, values);
        db.close();
    }

    public MealPlan getMealPlan(String date)
    {
        SQLiteDatabase db = this.getReadableDatabase();


        Cursor cursor = db.query(TABLE_MEAL_PLANS, new String[] { KEY_ID, KEY_MEAL_PLAN_DATE, KEY_MEAL_PLAN_BREAKFAST, KEY_MEAL_PLAN_LUNCH, KEY_MEAL_PLAN_DINNER}, KEY_MEAL_PLAN_DATE + "=?",
                new String[] {date}, null, null, null, null);

        if(cursor != null) {
            cursor.moveToFirst();
        } else {
            return null;
        }

        MealPlan mealPlan = new MealPlan(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));

        return mealPlan;
    }

    public ArrayList<MealPlan> getAllMealPlans()
    {

        ArrayList<MealPlan> mealList = new ArrayList<MealPlan>();

        String selectQuery = "SELECT * FROM " + TABLE_MEAL_PLANS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst())
        {
            do {

                MealPlan mealPlan = new MealPlan(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
                mealList.add(mealPlan);

            } while (cursor.moveToNext());
        }

        return mealList;

    }

    /********** MEAL OPERATIONS **********/
    public void addMeal(Meal meal)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MEAL_NAME, meal.getMealName());
        values.put(KEY_MEAL_COUNT, meal.getMealCount());

        db.insert(TABLE_MEALS, null, values);

        db.close();
    }



    public Meal getMeal(String mealName)
    {
        SQLiteDatabase db = this.getReadableDatabase();


        Cursor cursor = db.query(TABLE_MEALS, new String[] { KEY_ID, KEY_MEAL_NAME, KEY_MEAL_COUNT}, KEY_MEAL_NAME + "=?",
                new String[] {mealName}, null, null, null, null);

        if(cursor != null) {
            cursor.moveToFirst();
        } else {
            return null;
        }

        Meal meal = new Meal();
        meal.setMealName(cursor.getString(1));
        meal.setMealCount(cursor.getInt(2));



        return meal;
    }

    public int updateMeal(Meal meal) {
        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues values = new ContentValues();
        values.put(KEY_MEAL_NAME, meal.getMealName());
        values.put(KEY_MEAL_COUNT, meal.getMealCount());

        // updating row
        return db.update(TABLE_MEALS, values, KEY_MEAL_NAME + " = ?",
                new String[] { meal.getMealName() });
    }

    public ArrayList<Meal> getAllMeals()
    {
        ArrayList<Meal> mealList = new ArrayList<Meal>();

        String selectQuery = "SELECT * FROM " + TABLE_MEALS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst())
        {
            do {
                Meal meal = new Meal();
                meal.setMealName(cursor.getString(1));
                meal.setMealCount(cursor.getInt(2));

                mealList.add(meal);

            } while (cursor.moveToNext());
        }

        return mealList;
    }

    /*********** OPERATIONS FOR INGREDIENTS TABLE ***********/
    public void addIngredient(Context context, String ingredient, String unit, int count)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_INGREDIENTS_NAME, ingredient);
        values.put(KEY_UNITS, unit);
        values.put(KEY_COUNT, count);

        try {
            db.insertOrThrow(TABLE_INGREDIENTS, null, values);
        } catch (SQLiteConstraintException e) {
            Log.v("Error", ingredient + " already exist in database");
        }

        db.close();
    }


    public ArrayList<Ingredient> getAllIngredients()
    {
        ArrayList<Ingredient> ingredientList = new ArrayList<Ingredient>();

        //SELECT ALL Query
        String selectQuery = "SELECT * FROM " + TABLE_INGREDIENTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        //Looping through all the rows and adding to the list
        if (cursor.moveToFirst()) {
            do {
                Ingredient ingredient = new Ingredient();
                ingredient.setIngredientName(cursor.getString(1));
                ingredient.setIngredientUnit(cursor.getString(2));
                ingredient.setIngredientCount(cursor.getInt(3));
               ingredientList.add(ingredient);
            } while (cursor.moveToNext());
        }

        return ingredientList;
    }

    /*********** OPERATIONS FOR RECIPE TABLE ***********/
    public boolean addRecipe(Context context, Recipe recipe)
    {
        boolean recipeAdded = true;
        SQLiteDatabase db = this.getWritableDatabase();
        //For the ArrayList of Recipes
        Gson gson = new Gson();
        String arrayListStr = gson.toJson(recipe.getIngredients());

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, recipe.getName());
        values.put(KEY_INGREDIENTS, arrayListStr);
        values.put(KEY_IMAGE_PATH, recipe.getImagePath());
        values.put(KEY_DESCRIPTION, recipe.getDescription());

        try {
            db.insertOrThrow(TABLE_RECIPES, null, values);
        } catch (SQLiteConstraintException e) { //If the value already exists, alert the user to enter a different recipe
            recipeAdded = false;
            Toast toast = Toast.makeText(context, "This recipe already exists, please try a different one", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();
        }

        db.close();
        return recipeAdded;
    }

    public Recipe getRecipe(int id)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_RECIPES, new String[] { KEY_ID, KEY_NAME, KEY_INGREDIENTS, KEY_IMAGE_PATH, KEY_DESCRIPTION}, KEY_ID + "=?",
                new String[] {String.valueOf(id)}, null, null, null, null);

        if(cursor != null) {
            cursor.moveToFirst();
        } else {
            return null;
        }

        Recipe recipe = new Recipe();
        recipe.setID(cursor.getInt(0));
        recipe.setName(cursor.getString(1));
        Gson gson = new Gson();
        ArrayList<Ingredient> ingredientList = gson.fromJson(cursor.getString(2), new TypeToken<ArrayList<Ingredient>>(){}.getType());
        recipe.setIngredients(ingredientList);
        recipe.setImagePath(cursor.getString(3));
        recipe.setDescription(cursor.getString(4));

        return recipe;
    }

    public Recipe getRecipe(String recipeName)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_RECIPES, new String[] { KEY_ID, KEY_NAME, KEY_INGREDIENTS, KEY_IMAGE_PATH, KEY_DESCRIPTION}, KEY_NAME + "=?",
                new String[] {recipeName}, null, null, null, null);

        if(cursor != null) {
            cursor.moveToFirst();
        } else {
            return null;
        }

        Recipe recipe = new Recipe();
        recipe.setID(cursor.getInt(0));
        recipe.setName(cursor.getString(1));
        Gson gson = new Gson();
        ArrayList<Ingredient> ingredientList = gson.fromJson(cursor.getString(2), new TypeToken<ArrayList<Ingredient>>(){}.getType());
        recipe.setIngredients(ingredientList);
        recipe.setImagePath(cursor.getString(3));
        recipe.setDescription(cursor.getString(4));

        return recipe;
    }

    public int updateRecipe(Recipe recipe) {
        SQLiteDatabase db = this.getWritableDatabase();

        Gson gson = new Gson();
        String arrayListStr = gson.toJson(recipe.getIngredients());

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, recipe.getName());
        values.put(KEY_INGREDIENTS, arrayListStr);
        values.put(KEY_IMAGE_PATH, recipe.getImagePath());
        values.put(KEY_DESCRIPTION, recipe.getDescription());

        // updating row
        return db.update(TABLE_RECIPES, values, KEY_ID + " = ?",
                new String[] { String.valueOf(recipe.getID()) });
    }


    public ArrayList<Recipe> getAllRecipes()
    {
        ArrayList<Recipe> recipeList = new ArrayList<Recipe>();

        //SELECT ALL Query
        String selectQuery = "SELECT * FROM " + TABLE_RECIPES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        //Looping through all the rows and adding to the list
        if (cursor.moveToFirst()) {
            do {
                Gson gson = new Gson();
                Recipe recipe = new Recipe();
                recipe.setID(Integer.parseInt(cursor.getString(0)));
                recipe.setName(cursor.getString(1));
                //Retrieve the token of ArrayList of ingredients
                ArrayList<Ingredient> ingredientList = gson.fromJson(cursor.getString(2), new TypeToken<ArrayList<Ingredient>>(){}.getType());
                recipe.setIngredients(ingredientList);
                recipe.setImagePath(cursor.getString(3));
                recipe.setDescription(cursor.getString(4));

                recipeList.add(recipe);
            } while (cursor.moveToNext());
        }

        return recipeList;
    }
}
