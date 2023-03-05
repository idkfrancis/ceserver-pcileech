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

import java.util.List;

public class SelectedProcess {

    private final String _executableName;
    private final int _pid;
    private MemoryMap<VadInfo> _memoryMap;

    public SelectedProcess(int pid) {
        _pid = pid;
        _executableName = PciLeech.getProcessExecutableName(pid);
    }

    public byte[] readMemory(long address, int size) {
        try {
            return PciLeech.readMemory(
                    _pid,
                    address,
                    size,
                    VmmDllFlags.NOCACHE,
                    VmmDllFlags.NOCACHEPUT,
                    VmmDllFlags.NO_PREDICTIVE_READ,
                    VmmDllFlags.NOPAGING,
                    VmmDllFlags.NOPAGING_IO
            );
        }
        catch (PciLeechException ex) {
            return new byte[0];
        }
    }

    public void writeMemory(long address, byte[] bytes) {
        PciLeech.writeMemory(
                _pid,
                address,
                bytes
        );
    }

    @Override
    public String toString() {
        return _executableName;
    }

    public MemoryMap<VadInfo> getMemoryMap() {
        if (_memoryMap == null) {
            MemoryMap<VadInfo> memoryMap = new MemoryMap<>();
            List<VadInfo> vadInfos = PciLeech.getVad(_pid, true);
            for (VadInfo vadInfo : vadInfos) {
                long regionSize = vadInfo.getEnd() - vadInfo.getStart() + 1;
                if (regionSize < Integer.MAX_VALUE) {
                    try {
                        memoryMap.add(
                                new MemoryRegion<>(
                                        vadInfo,
                                        vadInfo.getStart(),
                                        regionSize
                                )
                        );
                    }
                    catch (PciLeechException ex) {
                        System.err.println("ERROR: " + ex.getMessage());
                    }
                }
            }
            _memoryMap = memoryMap;
        }
        return _memoryMap;
    }

    public String getExecutableName() {
        return _executableName;
    }

}
