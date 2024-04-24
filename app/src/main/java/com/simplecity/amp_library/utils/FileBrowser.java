package com.simplecity.amp_library.utils;

import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import com.simplecity.amp_library.R;
import com.simplecity.amp_library.interfaces.FileType;
import com.simplecity.amp_library.model.BaseFileObject;
import com.simplecity.amp_library.model.FileObject;
import com.simplecity.amp_library.model.FolderObject;
import com.simplecity.amp_library.model.TagInfo;
import com.simplecity.amp_library.utils.sorting.SortManager;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileBrowser {

    @Nullable
    private File currentDir;

    private SettingsManager settingsManager;

    public FileBrowser(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    /**
     * Loads the specified folder.
     *
     * @param directory The file object to points to the directory to load.
     * @return An {@link List<BaseFileObject>} object that holds the data of the specified directory.
     */
    @WorkerThread
    public List<BaseFileObject> loadDir(File directory) {
        ThreadUtils.ensureNotOnMainThread();
        currentDir = directory;

        List<BaseFileObject> folderObjects = new ArrayList<>();
        List<BaseFileObject> fileObjects = new ArrayList<>();

        File[] files = directory.listFiles(FileHelper.getAudioFilter());
        if (files != null) {
            processFiles(files, folderObjects, fileObjects);
        }

        sortAndAddObjects(folderObjects, fileObjects);

        return folderObjects;
    }

    private void processFiles(File[] files, List<BaseFileObject> folderObjects, List<BaseFileObject> fileObjects) {
        for (File file : files) {
            BaseFileObject baseFileObject = createBaseFileObject(file);
            if (baseFileObject != null) {
                if (baseFileObject instanceof FolderObject) {
                    folderObjects.add(baseFileObject);
                } else {
                    fileObjects.add(baseFileObject);
                }
            }
        }
    }

    private BaseFileObject createBaseFileObject(File file) {
        if (file.isDirectory()) {
            return createFolderObject(file);
        } else {
            return createFileObject(file);
        }
    }

    private FolderObject createFolderObject(File file) {
        FolderObject folderObject = new FolderObject();
        folderObject.path = FileHelper.getPath(file);
        folderObject.name = file.getName();
        File[] listOfFiles = file.listFiles(FileHelper.getAudioFilter());
        if (listOfFiles != null && listOfFiles.length > 0) {
            folderObject.folderCount = countFolders(listOfFiles);
            folderObject.fileCount = countFiles(listOfFiles);
        }
        return folderObject;
    }

    private int countFolders(File[] files) {
        int count = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                count++;
            }
        }
        return count;
    }

    private int countFiles(File[] files) {
        int count = 0;
        for (File file : files) {
            if (!file.isDirectory()) {
                count++;
            }
        }
        return count;
    }

    private FileObject createFileObject(File file) {
        FileObject fileObject = new FileObject();
        fileObject.path = FileHelper.getPath(file);
        fileObject.name = FileHelper.getName(file.getName());
        fileObject.size = file.length();
        fileObject.extension = FileHelper.getExtension(file.getName());
        if (TextUtils.isEmpty(fileObject.extension)) {
            return null;
        }
        fileObject.tagInfo = new TagInfo(fileObject.path);
        return fileObject;
    }

    private void sortAndAddObjects(List<BaseFileObject> folderObjects, List<BaseFileObject> fileObjects) {
        sortFileObjects(fileObjects);
        sortFolderObjects(folderObjects);

        if (!settingsManager.getFolderBrowserFilesAscending()) {
            Collections.reverse(fileObjects);
        }

        if (!settingsManager.getFolderBrowserFoldersAscending()) {
            Collections.reverse(folderObjects);
        }

        folderObjects.addAll(fileObjects);

        if (!FileHelper.isRootDirectory(currentDir)) {
            folderObjects.add(0, createParentObject());
        }
    }

    private FolderObject createParentObject() {
        FolderObject parentObject = new FolderObject();
        parentObject.fileType = FileType.PARENT;
        parentObject.name = FileHelper.PARENT_DIRECTORY;
        parentObject.path = FileHelper.getPath(currentDir) + FileHelper.PARENT_DIRECTORY;
        return parentObject;
    }

        sortFileObjects(fileObjects);
        sortFolderObjects(folderObjects);

        if (!settingsManager.getFolderBrowserFilesAscending()) {
            Collections.reverse(fileObjects);
        }

        if (!settingsManager.getFolderBrowserFoldersAscending()) {
            Collections.reverse(folderObjects);
        }

        folderObjects.addAll(fileObjects);

        if (!FileHelper.isRootDirectory(currentDir)) {
            FolderObject parentObject = new FolderObject();
            parentObject.fileType = FileType.PARENT;
            parentObject.name = FileHelper.PARENT_DIRECTORY;
            parentObject.path = FileHelper.getPath(currentDir) + FileHelper.PARENT_DIRECTORY;
            folderObjects.add(0, parentObject);
        }

        return folderObjects;
    }

    @Nullable
    public File getCurrentDir() {
        return currentDir;
    }

    @WorkerThread
    public File getInitialDir() {
        ThreadUtils.ensureNotOnMainThread();

        File dir = getInitialDirFromSettings();
        if (dir != null) {
            return dir;
        }

        dir = getInitialDirFromStorage();
        if (dir != null) {
            return dir;
        }

        return getDefaultInitialDir();
    }

    private File getInitialDirFromSettings() {
        String settingsDir = settingsManager.getFolderBrowserInitialDir();
        if (settingsDir != null) {
            File file = new File(settingsDir);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    private File getInitialDirFromStorage() {
        File dir = new File("/");
        String[] storageDirs = dir.list((dir1, filename) -> dir1.isDirectory() && filename.toLowerCase().contains("storage"));
    
        if (storageDirs == null || storageDirs.length == 0) {
            return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ? Environment.getExternalStorageDirectory() : null;
        }
    
        dir = new File(dir + storageDirs[0]);
        String[] extSdCardDirs = dir.list((dir1, filename) -> dir1.isDirectory() && filename.toLowerCase().contains("extsdcard"));
    
        if (extSdCardDirs != null && extSdCardDirs.length > 0) {
            dir = new File(dir + extSdCardDirs[0]);
        }
    
        String[] musicDirs = dir.list((dir1, filename) -> dir1.isDirectory() && filename.toLowerCase().contains("music"));
    
        if (musicDirs != null && musicDirs.length > 0) {
            dir = new File(dir + musicDirs[0]);
        }
    
        return dir;
    }

    private File getDefaultInitialDir() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return Environment.getExternalStorageDirectory();
        }
        return null;
    }

    public void sortFolderObjects(List<BaseFileObject> baseFileObjects) {

        switch (settingsManager.getFolderBrowserFoldersSortOrder()) {
            case SortManager.SortFolders.COUNT:
                Collections.sort(baseFileObjects, fileCountComparator());
                Collections.sort(baseFileObjects, folderCountComparator());
                break;

            case SortManager.SortFolders.DEFAULT:
            default:
                Collections.sort(baseFileObjects, filenameComparator());
                break;
        }
    }

    public void sortFileObjects(List<BaseFileObject> baseFileObjects) {
        switch (settingsManager.getFolderBrowserFilesSortOrder()) {
            case SortManager.SortFiles.SIZE:
                Collections.sort(baseFileObjects, sizeComparator());
                break;
            case SortManager.SortFiles.FILE_NAME:
                Collections.sort(baseFileObjects, filenameComparator());
                break;
            case SortManager.SortFiles.ARTIST_NAME:
                Collections.sort(baseFileObjects, artistNameComparator());
                break;
            case SortManager.SortFiles.ALBUM_NAME:
                Collections.sort(baseFileObjects, albumNameComparator());
                break;
            case SortManager.SortFiles.TRACK_NAME:
                Collections.sort(baseFileObjects, trackNameComparator());
                break;
            case SortManager.SortFiles.DEFAULT:
            default:
                Collections.sort(baseFileObjects, trackNumberComparator());
                Collections.sort(baseFileObjects, albumNameComparator());
                Collections.sort(baseFileObjects, artistNameComparator());
                break;
        }
    }

    public void clearHomeDir() {
        settingsManager.setFolderBrowserInitialDir("");
    }

    public void setHomeDir() {
        if (currentDir != null) {
            settingsManager.setFolderBrowserInitialDir(currentDir.getPath());
        }
    }

    public File getHomeDir() {
        return new File(settingsManager.getFolderBrowserInitialDir());
    }

    public boolean hasHomeDir() {
        return !TextUtils.isEmpty(getHomeDir().getPath());
    }

    public boolean atHomeDirectory() {
        final File currDir = getCurrentDir();
        final File homeDir = getHomeDir();
        return currDir != null && homeDir != null && currDir.compareTo(homeDir) == 0;
    }

    public int getHomeDirIcon() {
        int icon = R.drawable.ic_folder_outline;
        if (atHomeDirectory()) {
            icon = R.drawable.ic_folder_remove;
        } else if (hasHomeDir()) {
            icon = R.drawable.ic_folder_nav;
        }
        return icon;
    }

    public int getHomeDirTitle() {
        int title = R.string.set_home_dir;
        if (atHomeDirectory()) {
            title = R.string.remove_home_dir;
        } else if (hasHomeDir()) {
            title = R.string.nav_home_dir;
        }
        return title;
    }

    private Comparator<BaseFileObject> sizeComparator() {
        return (Comparator<BaseFileObject>) (lhs, rhs) -> (int) (rhs.size - lhs.size);
    }

    private Comparator<BaseFileObject> filenameComparator() {
        return (Comparator<BaseFileObject>) (lhs, rhs) -> lhs.name.compareToIgnoreCase(rhs.name);
    }

    private Comparator<BaseFileObject> trackNumberComparator() {
        return (Comparator<FileObject>) (lhs, rhs) -> lhs.tagInfo.trackNumber - rhs.tagInfo.trackNumber;
    }

    private Comparator<BaseFileObject> folderCountComparator() {
        return (Comparator<FolderObject>) (lhs, rhs) -> rhs.folderCount - lhs.folderCount;
    }

    private Comparator<BaseFileObject> fileCountComparator() {
        return (Comparator<FolderObject>) (lhs, rhs) -> rhs.fileCount - lhs.fileCount;
    }

    private Comparator<BaseFileObject> artistNameComparator() {
        return (Comparator<FileObject>) (lhs, rhs) -> {
            if (lhs.tagInfo.artistName == null || rhs.tagInfo.artistName == null) {
                return nullCompare(lhs.tagInfo.artistName, rhs.tagInfo.artistName);
            }
            return lhs.tagInfo.artistName.compareToIgnoreCase(rhs.tagInfo.artistName);
        };
    }

    private Comparator<BaseFileObject> albumNameComparator() {
        return (Comparator<FileObject>) (lhs, rhs) -> {
            if (lhs.tagInfo.albumName == null || rhs.tagInfo.albumName == null) {
                return nullCompare(lhs.tagInfo.albumName, rhs.tagInfo.albumName);
            }
            return lhs.tagInfo.albumName.compareToIgnoreCase(rhs.tagInfo.albumName);
        };
    }

    private Comparator<BaseFileObject> trackNameComparator() {
        return (Comparator<FileObject>) (lhs, rhs) -> {
            if (lhs.tagInfo.trackName == null || rhs.tagInfo.trackName == null) {
                return nullCompare(lhs.tagInfo.trackName, rhs.tagInfo.trackName);
            }
            return lhs.tagInfo.trackName.compareToIgnoreCase(rhs.tagInfo.trackName);
        };
    }

    <T extends Comparable<T>> int nullCompare(T a, T b) {
        if (a == null && b == null) {
            return 0;
        } else if (a == null) {
            return Integer.MIN_VALUE;
        } else if (b == null) {
            return Integer.MAX_VALUE;
        } else {
            return a.compareTo(b);
        }
    }
}
