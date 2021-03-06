package org.cyclops.integrateddynamics.core.recipe.xml;

import com.google.common.collect.Lists;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.init.RecipeHandler;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientRecipeComponent;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientsAndFluidStackRecipeComponent;
import org.cyclops.cyclopscore.recipe.xml.SuperRecipeTypeHandler;
import org.cyclops.cyclopscore.recipe.xml.XmlRecipeLoader;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.block.BlockMechanicalSqueezer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

/**
 * Handler for mechanical squeezer recipes.
 * @author rubensworks
 *
 */
public class MechanicalSqueezerRecipeTypeHandler extends SuperRecipeTypeHandler<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DurationRecipeProperties> {

    @Override
    public String getCategoryId() {
        return Reference.MOD_ID + ":mechanical_squeezer_recipe";
    }

	@Override
	protected IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, DurationRecipeProperties> handleRecipe(RecipeHandler recipeHandler, Element input, Element output, Element properties)
			throws XmlRecipeLoader.XmlRecipeException {
        Ingredient inputItem = Ingredient.EMPTY;
        List<IngredientRecipeComponent> outputItems = Lists.newArrayList();
        FluidStack outputFluid = null;

        if(input.getElementsByTagName("item").getLength() > 0) {
            inputItem = getIngredient(recipeHandler, input.getElementsByTagName("item").item(0));
        }

        if(output.getElementsByTagName("item").getLength() > 0) {
            for (int i = 0; i < output.getElementsByTagName("item").getLength(); i++) {
                Node outputItemNode = output.getElementsByTagName("item").item(i);
                Ingredient outputItemIngredient = getIngredient(recipeHandler, outputItemNode);
                float outputItemChance = getChance(recipeHandler, outputItemNode);
                IngredientRecipeComponent outputItem = new IngredientRecipeComponent(outputItemIngredient);
                outputItem.setChance(outputItemChance);
                outputItems.add(outputItem);
            }
        }
        int duration = Integer.parseInt(properties.getElementsByTagName("duration").item(0).getTextContent());
        if(output.getElementsByTagName("fluid").getLength() > 0) {
            outputFluid = getFluid(recipeHandler, output.getElementsByTagName("fluid").item(0));
        }

        if(inputItem == null && outputFluid == null) {
            throw new XmlRecipeLoader.XmlRecipeException("Mechanical squeezer recipes must have an output item or fluid.");
        }

        IngredientRecipeComponent inputRecipeComponent = new IngredientRecipeComponent(inputItem);

        IngredientsAndFluidStackRecipeComponent outputRecipeComponent = new IngredientsAndFluidStackRecipeComponent(outputItems, outputFluid);

		return BlockMechanicalSqueezer.getInstance().getRecipeRegistry().registerRecipe(
                inputRecipeComponent,
                outputRecipeComponent,
                new DurationRecipeProperties(duration)
        );
	}

}
