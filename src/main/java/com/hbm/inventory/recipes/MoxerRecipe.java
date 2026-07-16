package com.hbm.inventory.recipes;

import com.hbm.inventory.recipes.loader.GenericRecipe;

public class MoxerRecipe extends GenericRecipe {

	public int amount;

	public MoxerRecipe(String name) { super(name); }

	public MoxerRecipe setAmount(int amount) { this.amount = amount; return this; }
}
