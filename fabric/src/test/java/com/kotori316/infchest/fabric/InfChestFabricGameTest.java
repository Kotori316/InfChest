package com.kotori316.infchest.fabric;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.test.DequeTest;
import com.kotori316.infchest.common.test.InfChestTest;
import com.kotori316.testutil.common.TestFunction;
import com.kotori316.testutil.common.TestFunctionRegister;
import net.fabricmc.api.ModInitializer;

public final class InfChestFabricGameTest implements ModInitializer {
    @Override
    public void onInitialize() {
        RecipeTest.recipeTests().forEach(TestFunctionRegister::registerTestFunction);
        DequeTest.functions(InfChest.modID + ":test", "fabric-gametest-api-v1:empty")
            .map(r -> TestFunction.createWithStructure(InfChest.modID, r.batchName(), r.name(), r.structure(), r.function()))
            .forEach(TestFunctionRegister::registerTestFunction);
        InfChestTest.functions(InfChest.modID + ":test", "fabric-gametest-api-v1:empty")
            .map(r -> TestFunction.createWithStructure(InfChest.modID, r.batchName(), r.name(), r.structure(), r.function()))
            .forEach(TestFunctionRegister::registerTestFunction);
        TestFunctionRegister.addFunctionsToRegistry(InfChest.modID, TestFunctionRegister::vanillaTestFunctionRegister);
    }
}
