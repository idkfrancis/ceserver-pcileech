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

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Win32Exception;

public class Win32Utils {

    public static void throwLastWin32Exception() {
        throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
    }

    public static String getLastPathComponent(String path) {
        if (path == null) {
            return null;
        }
        int idx = path.lastIndexOf('\\');
        if (idx >= 0) {
            path = path.substring(idx + 1);
        }
        return path;
    }
}
