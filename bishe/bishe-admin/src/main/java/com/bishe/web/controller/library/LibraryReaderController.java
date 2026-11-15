package com.bishe.web.controller.library;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.bishe.common.annotation.Log;
import com.bishe.common.core.controller.BaseController;
import com.bishe.common.core.domain.AjaxResult;
import com.bishe.common.core.page.TableDataInfo;
import com.bishe.common.enums.BusinessType;
import com.bishe.common.utils.SecurityUtils;
import com.bishe.common.utils.poi.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bishe.system.domain.LibraryReader;
import com.bishe.system.service.ILibraryReaderService;

@RestController
@RequestMapping("/library/reader")
public class LibraryReaderController extends BaseController
{
    @Autowired
    private ILibraryReaderService libraryReaderService;

    @PreAuthorize("@ss.hasPermi('library:reader:list')")
    @GetMapping("/list")
    public TableDataInfo list(LibraryReader reader)
    {
        startPage();
        List<LibraryReader> list = libraryReaderService.selectLibraryReaderList(reader);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('library:reader:export')")
    @Log(title = "读者信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LibraryReader reader)
    {
        List<LibraryReader> list = libraryReaderService.selectLibraryReaderList(reader);
        ExcelUtil<LibraryReader> util = new ExcelUtil<LibraryReader>(LibraryReader.class);
        util.exportExcel(response, list, "读者信息数据");
    }

    @PreAuthorize("@ss.hasPermi('library:reader:query')")
    @GetMapping(value = "/{readerId}")
    public AjaxResult getInfo(@PathVariable Long readerId)
    {
        return success(libraryReaderService.selectLibraryReaderById(readerId));
    }

    @PreAuthorize("@ss.hasPermi('library:reader:add')")
    @Log(title = "读者信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LibraryReader reader)
    {
        reader.setCreateBy(getUsername());
        return toAjax(libraryReaderService.insertLibraryReader(reader));
    }

    @PreAuthorize("@ss.hasPermi('library:reader:edit')")
    @Log(title = "读者信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LibraryReader reader)
    {
        reader.setUpdateBy(getUsername());
        return toAjax(libraryReaderService.updateLibraryReader(reader));
    }

    @PreAuthorize("@ss.hasPermi('library:reader:remove')")
    @Log(title = "读者信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{readerIds}")
    public AjaxResult remove(@PathVariable Long[] readerIds)
    {
        return toAjax(libraryReaderService.deleteLibraryReaderByIds(readerIds));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public AjaxResult profile()
    {
        return success(libraryReaderService.ensureLibraryReaderByUserId(SecurityUtils.getUserId()));
    }
}
