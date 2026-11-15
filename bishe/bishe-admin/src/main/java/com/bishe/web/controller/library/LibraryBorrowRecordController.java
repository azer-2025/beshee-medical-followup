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

import com.bishe.system.domain.LibraryBorrowRecord;
import com.bishe.system.service.ILibraryBorrowRecordService;

@RestController
@RequestMapping("/library/borrow")
public class LibraryBorrowRecordController extends BaseController
{
    @Autowired
    private ILibraryBorrowRecordService libraryBorrowRecordService;

    @PreAuthorize("@ss.hasPermi('library:borrow:list')")
    @GetMapping("/list")
    public TableDataInfo list(LibraryBorrowRecord record)
    {
        startPage();
        List<LibraryBorrowRecord> list = libraryBorrowRecordService.selectLibraryBorrowRecordList(record);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('library:borrow:export')")
    @Log(title = "借阅信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LibraryBorrowRecord record)
    {
        List<LibraryBorrowRecord> list = libraryBorrowRecordService.selectLibraryBorrowRecordList(record);
        ExcelUtil<LibraryBorrowRecord> util = new ExcelUtil<LibraryBorrowRecord>(LibraryBorrowRecord.class);
        util.exportExcel(response, list, "借阅信息数据");
    }

    @PreAuthorize("@ss.hasPermi('library:borrow:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable Long recordId)
    {
        return success(libraryBorrowRecordService.selectLibraryBorrowRecordById(recordId));
    }

    @PreAuthorize("@ss.hasPermi('library:borrow:add')")
    @Log(title = "借阅信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LibraryBorrowRecord record)
    {
        record.setCreateBy(getUsername());
        return toAjax(libraryBorrowRecordService.insertLibraryBorrowRecord(record));
    }

    @PreAuthorize("@ss.hasPermi('library:borrow:edit')")
    @Log(title = "借阅信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LibraryBorrowRecord record)
    {
        record.setUpdateBy(getUsername());
        return toAjax(libraryBorrowRecordService.updateLibraryBorrowRecord(record));
    }

    @PreAuthorize("@ss.hasPermi('library:borrow:return')")
    @Log(title = "借阅归还", businessType = BusinessType.UPDATE)
    @PostMapping("/return/{recordId}")
    public AjaxResult returnBook(@PathVariable Long recordId)
    {
        return toAjax(libraryBorrowRecordService.returnLibraryBorrowRecord(recordId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('library:borrow:renew')")
    @Log(title = "借阅续借", businessType = BusinessType.UPDATE)
    @PostMapping("/renew/{recordId}")
    public AjaxResult renewBook(@PathVariable Long recordId)
    {
        return toAjax(libraryBorrowRecordService.renewLibraryBorrowRecord(recordId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('library:borrow:remind')")
    @Log(title = "逾期催还", businessType = BusinessType.UPDATE)
    @PostMapping("/remind/{recordId}")
    public AjaxResult remindBook(@PathVariable Long recordId)
    {
        return toAjax(libraryBorrowRecordService.remindLibraryBorrowRecord(recordId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('library:borrow:remove')")
    @Log(title = "借阅信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        return toAjax(libraryBorrowRecordService.deleteLibraryBorrowRecordByIds(recordIds));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my/list")
    public TableDataInfo myList()
    {
        startPage();
        List<LibraryBorrowRecord> list = libraryBorrowRecordService.selectMyBorrowRecordList(SecurityUtils.getUserId());
        return getDataTable(list);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/my/borrow/{bookId}")
    public AjaxResult myBorrow(@PathVariable Long bookId)
    {
        return toAjax(libraryBorrowRecordService.borrowBookByUser(SecurityUtils.getUserId(), bookId));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/my/return/{recordId}")
    public AjaxResult myReturn(@PathVariable Long recordId)
    {
        return toAjax(libraryBorrowRecordService.returnBookByUser(SecurityUtils.getUserId(), recordId));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/my/renew/{recordId}")
    public AjaxResult myRenew(@PathVariable Long recordId)
    {
        return toAjax(libraryBorrowRecordService.renewBookByUser(SecurityUtils.getUserId(), recordId));
    }
}
