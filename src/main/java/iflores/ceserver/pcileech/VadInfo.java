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

import java.io.Serializable;

public class VadInfo implements Serializable {

    private final String _name;
    private final long _start;
    private final long _end;
    private final int _type;
    private final int _protection;
    private final int _fImage;
    private final int _fFile;
    private final int _fPageFile;
    private final int _fPrivateMemory;
    private final int _fTeb;
    private final int _fStack;
    private final int _fSpare;
    private final int _HeapNum;
    private final int _fHeap;
    private final int _cwszDescription;
    private final int _commitCharge;
    private final int _memCommit;

    public VadInfo(String name, long start, long end, int dword0, int dword1) {
        _name = name;
        _start = start;
        _end = end;

        _type = getValue(dword0, 0, 3);
        _protection = getValue(dword0, 3, 5);
        _fImage = getValue(dword0, 8, 1);
        _fFile = getValue(dword0, 9, 1);
        _fPageFile = getValue(dword0, 10, 1);
        _fPrivateMemory = getValue(dword0, 11, 1);
        _fTeb = getValue(dword0, 12, 1);
        _fStack = getValue(dword0, 13, 1);
        _fSpare = getValue(dword0, 14, 2);
        _HeapNum = getValue(dword0, 16, 7);
        _fHeap = getValue(dword0, 23, 1);
        _cwszDescription = getValue(dword0, 24, 8);
        _commitCharge = getValue(dword1, 0, 31);
        _memCommit = getValue(dword1, 31, 1);
    }

    public String getName() {
        return _name;
    }

    public long getStart() {
        return _start;
    }

    public long getEnd() {
        return _end;
    }

    @Override
    public String toString() {
        return _name + " (" + Long.toHexString(_start) + "-" + Long.toHexString(_end) + ")";
    }

    private int getValue(int mask, int start, int length) {
        return (mask << (32 - start - length)) >>> (32 - length);
    }


    public int getProtection() {
        return _protection;
    }

    public int getfImage() {
        return _fImage;
    }

    public int getfFile() {
        return _fFile;
    }

    public int getfPageFile() {
        return _fPageFile;
    }

    public int getfPrivateMemory() {
        return _fPrivateMemory;
    }

    public int getfTeb() {
        return _fTeb;
    }

    public int getfStack() {
        return _fStack;
    }

    public int getfSpare() {
        return _fSpare;
    }

    public int getHeapNum() {
        return _HeapNum;
    }

    public int getfHeap() {
        return _fHeap;
    }

    public int getCwszDescription() {
        return _cwszDescription;
    }

    public int getCommitCharge() {
        return _commitCharge;
    }

    public int getMemCommit() {
        return _memCommit;
    }

    public int getType() {
        return _type;
    }

    public int getWin32Type() {
        switch (_type) {
            case 2:
                return Win32Constants.TYPE_MEM_IMAGE;
            case 1:
                return Win32Constants.TYPE_MEM_MAPPED;
            default:
                return Win32Constants.TYPE_MEM_PRIVATE;
        }
    }

    public int getWin32Protection() {
        return Win32Constants.PAGE_READWRITE;
        /*
        0  	MM_ZERO_ACCESS
        1	MM_READONLY
        2	MM_EXECUTE
        3	MM_EXECUTE_READ
        4  	MM_READWRITE
        5	MM_WRITECOPY
        6	MM_EXECUTE_READWRITE
        7	MM_EXECUTE_WRITECOPY
        */
//        switch (_protection) {
//            case 1: return Win32Constants.PAGE_READONLY;
//            case 2: return Win32Constants.PAGE_EXECUTE;
//            case 3: return Win32Constants.PAGE_EXECUTE_READ;
//            case 4: return Win32Constants.PAGE_READWRITE;
//            case 5: return Win32Constants.PAGE_WRITECOPY;
//            case 6: return Win32Constants.PAGE_EXECUTE_READWRITE;
//            case 7: return Win32Constants.PAGE_EXECUTE_WRITECOPY;
//            default: return Win32Constants.PAGE_NOACCESS;
//        }
    }
}
