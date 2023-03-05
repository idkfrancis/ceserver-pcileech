/*
 * This file is part of ceserver-pcileech by Isabella Flores
 *
 * Copyright © 2021-2022 Isabella Flores
 *
 * It is licensed to you under the terms of the
 * GNU Affero General Public License, Version 3.0.
 * Please see the file LICENSE for more information.
 */

package iflores.ceserver.pcileech;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class ToolHelp32Snapshot_Modules {

    private final SelectedProcess _selectedProcess;
    private Iterator<MemoryRegion<VadInfo>> _moduleInfoIterator;

    public ToolHelp32Snapshot_Modules(SelectedProcess selectedProcess) {
        _selectedProcess = selectedProcess;
    }

    public boolean hasNextModuleInfo() {
        return _moduleInfoIterator.hasNext();
    }

    public MemoryRegion<VadInfo> nextModuleInfo() {
        return _moduleInfoIterator.next();
    }

    public void restartModuleInfo() {
        List<MemoryRegion<VadInfo>> results = new ArrayList<>();
        for (MemoryRegion<VadInfo> memoryRegion : _selectedProcess.getMemoryMap()) {
            VadInfo vadInfo = memoryRegion.getUserObject();
            if (vadInfo.getfImage() != 0) {
                results.add(memoryRegion);
            }
        }
        // Move the exe module to the top, since it gets shown as the default by Cheat Engine memory viewer
        String executableName = Win32Utils.getLastPathComponent(_selectedProcess.getExecutableName());
        results.sort(Comparator.comparing(x -> ! executableName.equalsIgnoreCase(Win32Utils.getLastPathComponent(x.getUserObject().getName()))));
        _moduleInfoIterator = results.iterator();
    }

}
