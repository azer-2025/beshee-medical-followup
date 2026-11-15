package com.bishe.web.controller.library;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.bishe.common.annotation.Log;
import com.bishe.common.core.controller.BaseController;
import com.bishe.common.core.domain.AjaxResult;
import com.bishe.common.core.page.TableDataInfo;
import com.bishe.common.enums.BusinessType;
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

import com.bishe.system.domain.LibraryBook;
import com.bishe.system.service.ILibraryBookService;

@RestController
@RequestMapping("/library/book")
public class LibraryBookController extends BaseController
{
    @Autowired
    private ILibraryBookService libraryBookService;

    @PreAuthorize("@ss.hasPermi('library:book:list')")
    @GetMapping("/list")
    public TableDataInfo list(LibraryBook book)
    {
        startPage();
        List<LibraryBook> list = libraryBookService.selectLibraryBookList(book);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('library:book:export')")
    @Log(title = "图书信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LibraryBook book)
    {
        List<LibraryBook> list = libraryBookService.selectLibraryBookList(book);
        ExcelUtil<LibraryBook> util = new ExcelUtil<LibraryBook>(LibraryBook.class);
        util.exportExcel(response, list, "图书信息数据");
    }

    @PreAuthorize("@ss.hasPermi('library:book:query')")
    @GetMapping(value = "/{bookId}")
    public AjaxResult getInfo(@PathVariable Long bookId)
    {
        return success(libraryBookService.selectLibraryBookById(bookId));
    }

    @PreAuthorize("@ss.hasPermi('library:book:add')")
    @Log(title = "图书信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LibraryBook book)
    {
        book.setCreateBy(getUsername());
        return toAjax(libraryBookService.insertLibraryBook(book));
    }

    @PreAuthorize("@ss.hasPermi('library:book:edit')")
    @Log(title = "图书信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LibraryBook book)
    {
        book.setUpdateBy(getUsername());
        return toAjax(libraryBookService.updateLibraryBook(book));
    }

    @PreAuthorize("@ss.hasPermi('library:book:remove')")
    @Log(title = "图书信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{bookIds}")
    public AjaxResult remove(@PathVariable Long[] bookIds)
    {
        return toAjax(libraryBookService.deleteLibraryBookByIds(bookIds));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/portal/list")
    public TableDataInfo portalList(LibraryBook book)
    {
        startPage();
        List<LibraryBook> list = libraryBookService.selectPortalBookList(book);
        return getDataTable(list);
    }
}
