/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import ivorius.reccomplex.structures.*;
import ivorius.reccomplex.worldgen.StructureGenerator;
import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.operation.OperationRegistry;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.utils.ServerTranslations;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandImportStructure extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "import";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.strucImport.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
    {
        if (args.length <= 0)
        {
            throw ServerTranslations.wrongUsageException("commands.strucImport.usage");
        }

        String structureID = args[0];
        StructureInfo<?> structureInfo = StructureRegistry.INSTANCE.getStructure(structureID);
        World world = commandSender.getEntityWorld();

        if (structureInfo == null)
        {
            throw ServerTranslations.commandException("commands.strucImport.noStructure", structureID);
        }

        BlockPos coord;

        if (args.length >= 4)
            coord = parseBlockPos(commandSender, args, 1, false);
        else
            coord = commandSender.getPosition();

        int rotation = args.length >= 5 ? parseInt(args[4]) : 0;
        boolean mirror = args.length >= 6 && parseBoolean(args[5]);
        AxisAlignedTransform2D transform = AxisAlignedTransform2D.from(rotation, mirror);

        if (structureInfo instanceof GenericStructureInfo)
            OperationRegistry.queueOperation(new OperationGenerateStructure((GenericStructureInfo) structureInfo, structureID, transform, coord, true, structureID), commandSender);
        else
        {
            new StructureGenerator<>(structureInfo).world((WorldServer) world)
                    .transform(transform).lowerCoord(coord).asSource(true).generate();
        }
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, StructureRegistry.INSTANCE.allStructureIDs());
        else if (args.length == 2 || args.length == 3 || args.length == 4)
            return getListOfStringsMatchingLastWord(args, "~");
        else if (args.length == 5)
            return getListOfStringsMatchingLastWord(args, "0", "1", "2", "3");
        else if (args.length == 6)
            return getListOfStringsMatchingLastWord(args, "true", "false");

        return Collections.emptyList();
    }
}
