/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.client.rendering.MazeVisualizationContext;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.TableElementSaveDirectory;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.*;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSupplied;
import ivorius.reccomplex.utils.SaveDirectoryData;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.Structures;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceGenericStructure extends TableDataSourceSegmented
{
    protected GenericStructure structureInfo;
    protected String structureKey;

    protected SaveDirectoryData saveDirectoryData;

    protected TableDelegate tableDelegate;
    protected TableNavigator navigator;

    public TableDataSourceGenericStructure(GenericStructure structureInfo, String structureKey, SaveDirectoryData saveDirectoryData, TableDelegate delegate, TableNavigator navigator, MazeVisualizationContext visualizationContext)
    {
        this.structureInfo = structureInfo;
        this.structureKey = structureKey;
        this.saveDirectoryData = saveDirectoryData;
        this.tableDelegate = delegate;
        this.navigator = navigator;

        addManagedSegment(1, new TableDataSourceSupplied(() -> TableElementSaveDirectory.create(saveDirectoryData, () -> structureKey, delegate)));

        addManagedSegment(2, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceMetadata(structureInfo.metadata))
                .buildDataSource(IvTranslations.get("reccomplex.structure.metadata"), IvTranslations.getLines("reccomplex.structure.metadata.tooltip")));

        addManagedSegment(4, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceGenerationType(structureInfo.generationTypes, visualizationContext, delegate, navigator))
                .buildDataSource(IvTranslations.get("reccomplex.structure.generation"), IvTranslations.getLines("reccomplex.structure.generation.tooltip")));

        addManagedSegment(5, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> structureInfo.transformer.tableDataSource(navigator, delegate))
                .buildDataSource(IvTranslations.get("reccomplex.structure.transformers"), IvTranslations.getLines("reccomplex.structure.transformers.tooltip")));

        addManagedSegment(6, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceGenericVariableDomain(delegate, navigator, structureInfo.variableDomain))
                .buildDataSource(IvTranslations.get("reccomplex.structure.variables"), IvTranslations.getLines("reccomplex.structure.variables.tooltip")));

        addManagedSegment(7, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.structure.dependencies"), IvTranslations.getLines("reccomplex.structure.dependencies.tooltip"), structureInfo.dependencies, RecurrentComplex.saver));
    }

    public GenericStructure getStructureInfo()
    {
        return structureInfo;
    }

    public void setStructureInfo(GenericStructure structureInfo)
    {
        this.structureInfo = structureInfo;
    }

    public String getStructureKey()
    {
        return structureKey;
    }

    public void setStructureKey(String structureKey)
    {
        this.structureKey = structureKey;
    }

    public SaveDirectoryData getSaveDirectoryData()
    {
        return saveDirectoryData;
    }

    public void setSaveDirectoryData(SaveDirectoryData saveDirectoryData)
    {
        this.saveDirectoryData = saveDirectoryData;
    }

    public TableDelegate getTableDelegate()
    {
        return tableDelegate;
    }

    public void setTableDelegate(TableDelegate tableDelegate)
    {
        this.tableDelegate = tableDelegate;
    }

    public TableNavigator getNavigator()
    {
        return navigator;
    }

    public void setNavigator(TableNavigator navigator)
    {
        this.navigator = navigator;
    }

    @Override
    @Nonnull
    public String title()
    {
        return "Generic Structure";
    }

    @Override
    public int numberOfSegments()
    {
        return 8;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 0:
                return 1;
            case 3:
                return 2;
        }

        return super.sizeOfSegment(segment);
    }

    @Override
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        switch (segment)
        {
            case 0:
                if (index == 0)
                {
                    TableCellString cell = new TableCellString(null, structureKey);
                    cell.addPropertyConsumer(cell1 ->
                    {
                        structureKey = cell.getPropertyValue();
                        cell.setValidityState(currentNameState());
                        TableCells.reloadExcept(tableDelegate, "structureID");
                    });
                    cell.setShowsValidityState(true);
                    cell.setValidityState(currentNameState());
                    return new TitledCell("structureID", IvTranslations.get("reccomplex.structure.id"), cell).withTitleTooltip(IvTranslations.formatLines("reccomplex.structure.id.tooltip"));
                }
            case 3:
            {
                if (index == 0)
                {
                    TableCellBoolean cellRotatable = new TableCellBoolean("rotatable", structureInfo.rotatable,
                            IvTranslations.get("reccomplex.structure.rotatable.true"),
                            IvTranslations.get("reccomplex.structure.rotatable.false"));
                    cellRotatable.setTooltip(IvTranslations.formatLines("reccomplex.structure.rotatable.tooltip"));
                    cellRotatable.addPropertyConsumer(cell -> structureInfo.rotatable = cellRotatable.getPropertyValue());

                    TableCellBoolean cellMirrorable = new TableCellBoolean("mirrorable", structureInfo.mirrorable,
                            IvTranslations.format("reccomplex.structure.mirrorable.true"),
                            IvTranslations.format("reccomplex.structure.mirrorable.false"));
                    cellMirrorable.setTooltip(IvTranslations.formatLines("reccomplex.structure.mirrorable.tooltip"));
                    cellMirrorable.addPropertyConsumer(cell -> structureInfo.mirrorable = cellMirrorable.getPropertyValue());

                    return new TitledCell(new TableCellMulti(cellRotatable, cellMirrorable));
                }
                else if (index == 1)
                {
                    TableCellBoolean cellBlocking = new TableCellBoolean("blocking", structureInfo.blocking,
                            IvTranslations.format("reccomplex.structure.blocking.true"),
                            IvTranslations.format("reccomplex.structure.blocking.false"));
                    cellBlocking.setTooltip(IvTranslations.formatLines("reccomplex.structure.blocking.tooltip"));
                    cellBlocking.addPropertyConsumer(cell -> structureInfo.blocking = cellBlocking.getPropertyValue());

                    return new TitledCell(cellBlocking);
                }
            }
        }

        return super.cellForIndexInSegment(table, index, segment);
    }

    private GuiValidityStateIndicator.State currentNameState()
    {
        return Structures.isSimpleID(structureKey)
                ? StructureRegistry.INSTANCE.ids().contains(structureKey)
                ? GuiValidityStateIndicator.State.SEMI_VALID
                : GuiValidityStateIndicator.State.VALID
                : GuiValidityStateIndicator.State.INVALID;
    }
}
