-- ----------------------------
-- 1、部门表
-- ----------------------------
drop table if exists sys_dept;
create table sys_dept (
                          dept_id           bigint(20)      not null auto_increment    comment '部门id',
                          parent_id         bigint(20)      default 0                  comment '父部门id',
                          ancestors         varchar(50)     default ''                 comment '祖级列表',
                          dept_name         varchar(30)     default ''                 comment '部门名称',
                          order_num         int(4)          default 0                  comment '显示顺序',
                          leader            varchar(20)     default null               comment '负责人',
                          phone             varchar(11)     default null               comment '联系电话',
                          email             varchar(50)     default null               comment '邮箱',
                          status            char(1)         default '0'                comment '部门状态（0正常 1停用）',
                          del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
                          create_by         varchar(64)     default ''                 comment '创建者',
                          create_time 	    datetime                                   comment '创建时间',
                          update_by         varchar(64)     default ''                 comment '更新者',
                          update_time       datetime                                   comment '更新时间',
                          primary key (dept_id)
) engine=innodb auto_increment=200 comment = '部门表';

-- ----------------------------
-- 初始化-部门表数据
-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.



-- ----------------------------
-- 2、用户信息表
-- ----------------------------
drop table if exists sys_user;
create table sys_user (
                          user_id           bigint(20)      not null auto_increment    comment '用户ID',
                          dept_id           bigint(20)      default null               comment '部门ID',
                          user_name         varchar(30)     not null                   comment '用户账号',
                          nick_name         varchar(30)     not null                   comment '用户昵称',
                          user_type         varchar(2)      default '00'               comment '用户类型（00系统用户）',
                          email             varchar(50)     default ''                 comment '用户邮箱',
                          phonenumber       varchar(11)     default ''                 comment '手机号码',
                          sex               char(1)         default '0'                comment '用户性别（0男 1女 2未知）',
                          avatar            varchar(100)    default ''                 comment '头像地址',
                          password          varchar(100)    default ''                 comment '密码',
                          status            char(1)         default '0'                comment '账号状态（0正常 1停用）',
                          del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
                          login_ip          varchar(128)    default ''                 comment '最后登录IP',
                          login_date        datetime                                   comment '最后登录时间',
                          pwd_update_date   datetime                                   comment '密码最后更新时间',
                          create_by         varchar(64)     default ''                 comment '创建者',
                          create_time       datetime                                   comment '创建时间',
                          update_by         varchar(64)     default ''                 comment '更新者',
                          update_time       datetime                                   comment '更新时间',
                          remark            varchar(500)    default null               comment '备注',
                          primary key (user_id)
) engine=innodb auto_increment=100 comment = '用户信息表';

-- ----------------------------
-- 初始化-用户信息表数据
-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.



-- ----------------------------
-- 3、岗位信息表
-- ----------------------------
drop table if exists sys_post;
create table sys_post
(
    post_id       bigint(20)      not null auto_increment    comment '岗位ID',
    post_code     varchar(64)     not null                   comment '岗位编码',
    post_name     varchar(50)     not null                   comment '岗位名称',
    post_sort     int(4)          not null                   comment '显示顺序',
    status        char(1)         not null                   comment '状态（0正常 1停用）',
    create_by     varchar(64)     default ''                 comment '创建者',
    create_time   datetime                                   comment '创建时间',
    update_by     varchar(64)     default ''			       comment '更新者',
    update_time   datetime                                   comment '更新时间',
    remark        varchar(500)    default null               comment '备注',
    primary key (post_id)
) engine=innodb comment = '岗位信息表';

-- ----------------------------
-- 初始化-岗位信息表数据
-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.



-- ----------------------------
-- 4、角色信息表
-- ----------------------------
drop table if exists sys_role;
create table sys_role (
                          role_id              bigint(20)      not null auto_increment    comment '角色ID',
                          role_name            varchar(30)     not null                   comment '角色名称',
                          role_key             varchar(100)    not null                   comment '角色权限字符串',
                          role_sort            int(4)          not null                   comment '显示顺序',
                          data_scope           char(1)         default '1'                comment '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
                          menu_check_strictly  tinyint(1)      default 1                  comment '菜单树选择项是否关联显示',
                          dept_check_strictly  tinyint(1)      default 1                  comment '部门树选择项是否关联显示',
                          status               char(1)         not null                   comment '角色状态（0正常 1停用）',
                          del_flag             char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
                          create_by            varchar(64)     default ''                 comment '创建者',
                          create_time          datetime                                   comment '创建时间',
                          update_by            varchar(64)     default ''                 comment '更新者',
                          update_time          datetime                                   comment '更新时间',
                          remark               varchar(500)    default null               comment '备注',
                          primary key (role_id)
) engine=innodb auto_increment=100 comment = '角色信息表';

-- ----------------------------
-- 初始化-角色信息表数据
-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.



-- ----------------------------
-- 5、菜单权限表
-- ----------------------------
drop table if exists sys_menu;
create table sys_menu (
                          menu_id           bigint(20)      not null auto_increment    comment '菜单ID',
                          menu_name         varchar(50)     not null                   comment '菜单名称',
                          parent_id         bigint(20)      default 0                  comment '父菜单ID',
                          order_num         int(4)          default 0                  comment '显示顺序',
                          path              varchar(200)    default ''                 comment '路由地址',
                          component         varchar(255)    default null               comment '组件路径',
                          query             varchar(255)    default null               comment '路由参数',
                          route_name        varchar(50)     default ''                 comment '路由名称',
                          is_frame          int(1)          default 1                  comment '是否为外链（0是 1否）',
                          is_cache          int(1)          default 0                  comment '是否缓存（0缓存 1不缓存）',
                          menu_type         char(1)         default ''                 comment '菜单类型（M目录 C菜单 F按钮）',
                          visible           char(1)         default 0                  comment '菜单状态（0显示 1隐藏）',
                          status            char(1)         default 0                  comment '菜单状态（0正常 1停用）',
                          perms             varchar(100)    default null               comment '权限标识',
                          icon              varchar(100)    default '#'                comment '菜单图标',
                          create_by         varchar(64)     default ''                 comment '创建者',
                          create_time       datetime                                   comment '创建时间',
                          update_by         varchar(64)     default ''                 comment '更新者',
                          update_time       datetime                                   comment '更新时间',
                          remark            varchar(500)    default ''                 comment '备注',
                          primary key (menu_id)
) engine=innodb auto_increment=2000 comment = '菜单权限表';

-- ----------------------------
-- 初始化-菜单信息表数据
-- ----------------------------
-- 一级菜单

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.

# insert into sys_menu values('3', '系统工具', '0', '3', 'tool',             null, '', '', 1, 0, 'M', '0', '0', '', 'tool',     'admin', sysdate(), '', null, '系统工具目录');
-- 二级菜单

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.

# insert into sys_menu values('103',  '部门管理', '1',   '4', 'dept',       'system/dept/index',        '', '', 1, 0, 'C', '0', '0', 'system:dept:list',        'tree',          'admin', sysdate(), '', null, '部门管理菜单');
# insert into sys_menu values('104',  '岗位管理', '1',   '5', 'post',       'system/post/index',        '', '', 1, 0, 'C', '0', '0', 'system:post:list',        'post',          'admin', sysdate(), '', null, '岗位管理菜单');

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.

# insert into sys_menu values('115',  '表单构建', '3',   '1', 'build',      'tool/build/index',         '', '', 1, 0, 'C', '0', '0', 'tool:build:list',         'build',         'admin', sysdate(), '', null, '表单构建菜单');
# insert into sys_menu values('116',  '代码生成', '3',   '2', 'gen',        'tool/gen/index',           '', '', 1, 0, 'C', '0', '0', 'tool:gen:list',           'code',          'admin', sysdate(), '', null, '代码生成菜单');

-- Data rows omitted from the public source snapshot.

-- 三级菜单

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.

-- 用户管理按钮

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.

-- 角色管理按钮

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.

-- 菜单管理按钮

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.

-- 部门管理按钮
# insert into sys_menu values('1016', '部门查询', '103', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query',          '#', 'admin', sysdate(), '', null, '');
# insert into sys_menu values('1017', '部门新增', '103', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add',            '#', 'admin', sysdate(), '', null, '');
# insert into sys_menu values('1018', '部门修改', '103', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit',           '#', 'admin', sysdate(), '', null, '');
# insert into sys_menu values('1019', '部门删除', '103', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove',         '#', 'admin', sysdate(), '', null, '');
# -- 岗位管理按钮
# insert into sys_menu values('1020', '岗位查询', '104', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:query',          '#', 'admin', sysdate(), '', null, '');
# insert into sys_menu values('1021', '岗位新增', '104', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:add',            '#', 'admin', sysdate(), '', null, '');
# insert into sys_menu values('1022', '岗位修改', '104', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit',           '#', 'admin', sysdate(), '', null, '');
# insert into sys_menu values('1023', '岗位删除', '104', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove',         '#', 'admin', sysdate(), '', null, '');
# insert into sys_menu values('1024', '岗位导出', '104', '5',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:export',         '#', 'admin', sysdate(), '', null, '');
-- 字典管理按钮

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.

-- 参数设置按钮

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.

-- 通知公告按钮

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.

-- 操作日志按钮

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.

-- 登录日志按钮

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.

-- 在线用户按钮

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.

-- 定时任务按钮

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.

-- 代码生成按钮

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.



-- ----------------------------
-- 6、用户和角色关联表  用户N-1角色
-- ----------------------------
drop table if exists sys_user_role;
create table sys_user_role (
                               user_id   bigint(20) not null comment '用户ID',
                               role_id   bigint(20) not null comment '角色ID',
                               primary key(user_id, role_id)
) engine=innodb comment = '用户和角色关联表';

-- ----------------------------
-- 初始化-用户和角色关联表数据
-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.



-- ----------------------------
-- 7、角色和菜单关联表  角色1-N菜单
-- ----------------------------
drop table if exists sys_role_menu;
create table sys_role_menu (
                               role_id   bigint(20) not null comment '角色ID',
                               menu_id   bigint(20) not null comment '菜单ID',
                               primary key(role_id, menu_id)
) engine=innodb comment = '角色和菜单关联表';

-- ----------------------------
-- 初始化-角色和菜单关联表数据
-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- ----------------------------
-- 8、角色和部门关联表  角色1-N部门
-- ----------------------------
drop table if exists sys_role_dept;
create table sys_role_dept (
                               role_id   bigint(20) not null comment '角色ID',
                               dept_id   bigint(20) not null comment '部门ID',
                               primary key(role_id, dept_id)
) engine=innodb comment = '角色和部门关联表';

-- ----------------------------
-- 初始化-角色和部门关联表数据
-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.



-- ----------------------------
-- 9、用户与岗位关联表  用户1-N岗位
-- ----------------------------
drop table if exists sys_user_post;
create table sys_user_post
(
    user_id   bigint(20) not null comment '用户ID',
    post_id   bigint(20) not null comment '岗位ID',
    primary key (user_id, post_id)
) engine=innodb comment = '用户与岗位关联表';

-- ----------------------------
-- 初始化-用户与岗位关联表数据
-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.



-- ----------------------------
-- 10、操作日志记录
-- ----------------------------
drop table if exists sys_oper_log;
create table sys_oper_log (
                              oper_id           bigint(20)      not null auto_increment    comment '日志主键',
                              title             varchar(50)     default ''                 comment '模块标题',
                              business_type     int(2)          default 0                  comment '业务类型（0其它 1新增 2修改 3删除）',
                              method            varchar(200)    default ''                 comment '方法名称',
                              request_method    varchar(10)     default ''                 comment '请求方式',
                              operator_type     int(1)          default 0                  comment '操作类别（0其它 1后台用户 2手机端用户）',
                              oper_name         varchar(50)     default ''                 comment '操作人员',
                              dept_name         varchar(50)     default ''                 comment '部门名称',
                              oper_url          varchar(255)    default ''                 comment '请求URL',
                              oper_ip           varchar(128)    default ''                 comment '主机地址',
                              oper_location     varchar(255)    default ''                 comment '操作地点',
                              oper_param        varchar(2000)   default ''                 comment '请求参数',
                              json_result       varchar(2000)   default ''                 comment '返回参数',
                              status            int(1)          default 0                  comment '操作状态（0正常 1异常）',
                              error_msg         varchar(2000)   default ''                 comment '错误消息',
                              oper_time         datetime                                   comment '操作时间',
                              cost_time         bigint(20)      default 0                  comment '消耗时间',
                              primary key (oper_id),
                              key idx_sys_oper_log_bt (business_type),
                              key idx_sys_oper_log_s  (status),
                              key idx_sys_oper_log_ot (oper_time)
) engine=innodb auto_increment=100 comment = '操作日志记录';


-- ----------------------------
-- 11、字典类型表
-- ----------------------------
drop table if exists sys_dict_type;
create table sys_dict_type
(
    dict_id          bigint(20)      not null auto_increment    comment '字典主键',
    dict_name        varchar(100)    default ''                 comment '字典名称',
    dict_type        varchar(100)    default ''                 comment '字典类型',
    status           char(1)         default '0'                comment '状态（0正常 1停用）',
    create_by        varchar(64)     default ''                 comment '创建者',
    create_time      datetime                                   comment '创建时间',
    update_by        varchar(64)     default ''                 comment '更新者',
    update_time      datetime                                   comment '更新时间',
    remark           varchar(500)    default null               comment '备注',
    primary key (dict_id),
    unique (dict_type)
) engine=innodb auto_increment=100 comment = '字典类型表';

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.



-- ----------------------------
-- 12、字典数据表
-- ----------------------------
drop table if exists sys_dict_data;
create table sys_dict_data
(
    dict_code        bigint(20)      not null auto_increment    comment '字典编码',
    dict_sort        int(4)          default 0                  comment '字典排序',
    dict_label       varchar(100)    default ''                 comment '字典标签',
    dict_value       varchar(100)    default ''                 comment '字典键值',
    dict_type        varchar(100)    default ''                 comment '字典类型',
    css_class        varchar(100)    default null               comment '样式属性（其他样式扩展）',
    list_class       varchar(100)    default null               comment '表格回显样式',
    is_default       char(1)         default 'N'                comment '是否默认（Y是 N否）',
    status           char(1)         default '0'                comment '状态（0正常 1停用）',
    create_by        varchar(64)     default ''                 comment '创建者',
    create_time      datetime                                   comment '创建时间',
    update_by        varchar(64)     default ''                 comment '更新者',
    update_time      datetime                                   comment '更新时间',
    remark           varchar(500)    default null               comment '备注',
    primary key (dict_code)
) engine=innodb auto_increment=1000 comment = '字典数据表';

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.



-- ----------------------------
-- 13、参数配置表
-- ----------------------------
drop table if exists sys_config;
create table sys_config (
                            config_id         int(5)          not null auto_increment    comment '参数主键',
                            config_name       varchar(100)    default ''                 comment '参数名称',
                            config_key        varchar(100)    default ''                 comment '参数键名',
                            config_value      varchar(500)    default ''                 comment '参数键值',
                            config_type       char(1)         default 'N'                comment '系统内置（Y是 N否）',
                            create_by         varchar(64)     default ''                 comment '创建者',
                            create_time       datetime                                   comment '创建时间',
                            update_by         varchar(64)     default ''                 comment '更新者',
                            update_time       datetime                                   comment '更新时间',
                            remark            varchar(500)    default null               comment '备注',
                            primary key (config_id)
) engine=innodb auto_increment=100 comment = '参数配置表';

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.



-- ----------------------------
-- 14、系统访问记录
-- ----------------------------
drop table if exists sys_logininfor;
create table sys_logininfor (
                                info_id        bigint(20)     not null auto_increment   comment '访问ID',
                                user_name      varchar(50)    default ''                comment '用户账号',
                                ipaddr         varchar(128)   default ''                comment '登录IP地址',
                                login_location varchar(255)   default ''                comment '登录地点',
                                browser        varchar(50)    default ''                comment '浏览器类型',
                                os             varchar(50)    default ''                comment '操作系统',
                                status         char(1)        default '0'               comment '登录状态（0成功 1失败）',
                                msg            varchar(255)   default ''                comment '提示消息',
                                login_time     datetime                                 comment '访问时间',
                                primary key (info_id),
                                key idx_sys_logininfor_s  (status),
                                key idx_sys_logininfor_lt (login_time)
) engine=innodb auto_increment=100 comment = '系统访问记录';


-- ----------------------------
-- 15、定时任务调度表
-- ----------------------------
drop table if exists sys_job;
create table sys_job (
                         job_id              bigint(20)    not null auto_increment    comment '任务ID',
                         job_name            varchar(64)   default ''                 comment '任务名称',
                         job_group           varchar(64)   default 'DEFAULT'          comment '任务组名',
                         invoke_target       varchar(500)  not null                   comment '调用目标字符串',
                         cron_expression     varchar(255)  default ''                 comment 'cron执行表达式',
                         misfire_policy      varchar(20)   default '3'                comment '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
                         concurrent          char(1)       default '1'                comment '是否并发执行（0允许 1禁止）',
                         status              char(1)       default '0'                comment '状态（0正常 1暂停）',
                         create_by           varchar(64)   default ''                 comment '创建者',
                         create_time         datetime                                 comment '创建时间',
                         update_by           varchar(64)   default ''                 comment '更新者',
                         update_time         datetime                                 comment '更新时间',
                         remark              varchar(500)  default ''                 comment '备注信息',
                         primary key (job_id, job_name, job_group)
) engine=innodb auto_increment=100 comment = '定时任务调度表';

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.



-- ----------------------------
-- 16、定时任务调度日志表
-- ----------------------------
drop table if exists sys_job_log;
create table sys_job_log (
                             job_log_id          bigint(20)     not null auto_increment    comment '任务日志ID',
                             job_name            varchar(64)    not null                   comment '任务名称',
                             job_group           varchar(64)    not null                   comment '任务组名',
                             invoke_target       varchar(500)   not null                   comment '调用目标字符串',
                             job_message         varchar(500)                              comment '日志信息',
                             status              char(1)        default '0'                comment '执行状态（0正常 1失败）',
                             exception_info      varchar(2000)  default ''                 comment '异常信息',
                             create_time         datetime                                  comment '创建时间',
                             primary key (job_log_id)
) engine=innodb comment = '定时任务调度日志表';


-- ----------------------------
-- 17、通知公告表
-- ----------------------------
drop table if exists sys_notice;
create table sys_notice (
                            notice_id         int(4)          not null auto_increment    comment '公告ID',
                            notice_title      varchar(50)     not null                   comment '公告标题',
                            notice_type       char(1)         not null                   comment '公告类型（1通知 2公告）',
                            notice_content    longblob        default null               comment '公告内容',
                            status            char(1)         default '0'                comment '公告状态（0正常 1关闭）',
                            create_by         varchar(64)     default ''                 comment '创建者',
                            create_time       datetime                                   comment '创建时间',
                            update_by         varchar(64)     default ''                 comment '更新者',
                            update_time       datetime                                   comment '更新时间',
                            remark            varchar(255)    default null               comment '备注',
                            primary key (notice_id)
) engine=innodb auto_increment=10 comment = '通知公告表';

-- ----------------------------
-- 初始化-公告信息表数据
-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.



-- ----------------------------
-- 18、代码生成业务表
-- ----------------------------
drop table if exists gen_table;
create table gen_table (
                           table_id          bigint(20)      not null auto_increment    comment '编号',
                           table_name        varchar(200)    default ''                 comment '表名称',
                           table_comment     varchar(500)    default ''                 comment '表描述',
                           sub_table_name    varchar(64)     default null               comment '关联子表的表名',
                           sub_table_fk_name varchar(64)     default null               comment '子表关联的外键名',
                           class_name        varchar(100)    default ''                 comment '实体类名称',
                           tpl_category      varchar(200)    default 'crud'             comment '使用的模板（crud单表操作 tree树表操作）',
                           tpl_web_type      varchar(30)     default ''                 comment '前端模板类型（element-ui模版 element-plus模版）',
                           package_name      varchar(100)                               comment '生成包路径',
                           module_name       varchar(30)                                comment '生成模块名',
                           business_name     varchar(30)                                comment '生成业务名',
                           function_name     varchar(50)                                comment '生成功能名',
                           function_author   varchar(50)                                comment '生成功能作者',
                           gen_type          char(1)         default '0'                comment '生成代码方式（0zip压缩包 1自定义路径）',
                           gen_path          varchar(200)    default '/'                comment '生成路径（不填默认项目路径）',
                           options           varchar(1000)                              comment '其它生成选项',
                           create_by         varchar(64)     default ''                 comment '创建者',
                           create_time 	    datetime                                   comment '创建时间',
                           update_by         varchar(64)     default ''                 comment '更新者',
                           update_time       datetime                                   comment '更新时间',
                           remark            varchar(500)    default null               comment '备注',
                           primary key (table_id)
) engine=innodb auto_increment=1 comment = '代码生成业务表';


-- ----------------------------
-- 19、代码生成业务表字段
-- ----------------------------
drop table if exists gen_table_column;
create table gen_table_column (
                                  column_id         bigint(20)      not null auto_increment    comment '编号',
                                  table_id          bigint(20)                                 comment '归属表编号',
                                  column_name       varchar(200)                               comment '列名称',
                                  column_comment    varchar(500)                               comment '列描述',
                                  column_type       varchar(100)                               comment '列类型',
                                  java_type         varchar(500)                               comment 'JAVA类型',
                                  java_field        varchar(200)                               comment 'JAVA字段名',
                                  is_pk             char(1)                                    comment '是否主键（1是）',
                                  is_increment      char(1)                                    comment '是否自增（1是）',
                                  is_required       char(1)                                    comment '是否必填（1是）',
                                  is_insert         char(1)                                    comment '是否为插入字段（1是）',
                                  is_edit           char(1)                                    comment '是否编辑字段（1是）',
                                  is_list           char(1)                                    comment '是否列表字段（1是）',
                                  is_query          char(1)                                    comment '是否查询字段（1是）',
                                  query_type        varchar(200)    default 'EQ'               comment '查询方式（等于、不等于、大于、小于、范围）',
                                  html_type         varchar(200)                               comment '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
                                  dict_type         varchar(200)    default ''                 comment '字典类型',
                                  sort              int                                        comment '排序',
                                  create_by         varchar(64)     default ''                 comment '创建者',
                                  create_time 	    datetime                                   comment '创建时间',
                                  update_by         varchar(64)     default ''                 comment '更新者',
                                  update_time       datetime                                   comment '更新时间',
                                  primary key (column_id)
) engine=innodb auto_increment=1 comment = '代码生成业务表字段';


-- ----------------------------
-- ----------------------------
-- Add Roles: Doctor, Patient
-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- ----------------------------
-- Add Menus
-- ----------------------------
-- Patient Management (Top Level)

-- Data rows omitted from the public source snapshot.


-- My Patients (Submenu)

-- Data rows omitted from the public source snapshot.


-- ----------------------------
-- Doctor-Patient Association Table
-- ----------------------------
drop table if exists sys_doctor_patient;
create table sys_doctor_patient (
                                    doctor_id bigint(20) not null comment '医生ID',
                                    patient_id bigint(20) not null comment '患者ID',
                                    create_time datetime comment '关联时间',
                                    primary key(doctor_id, patient_id)
) engine=innodb comment = '医生患者关联表';


-- ----------------------------
-- Graduation Project: Health Management System Tables
-- ----------------------------

-- ----------------------------
-- 1. Health Archive Table (Electronic Health Records)
-- ----------------------------
drop table if exists health_archive;
create table health_archive (
                                archive_id        bigint(20)      not null auto_increment    comment '档案ID',
                                user_id           bigint(20)      not null                   comment '患者ID',
                                basic_info        varchar(2000)   default ''                 comment '基本信息(JSON或文本)',
                                medical_history   varchar(2000)   default ''                 comment '既往病史',
                                allergy_history   varchar(2000)   default ''                 comment '过敏史',
                                create_by         varchar(64)     default ''                 comment '创建者',
                                create_time       datetime                                   comment '创建时间',
                                update_by         varchar(64)     default ''                 comment '更新者',
                                update_time       datetime                                   comment '更新时间',
                                remark            varchar(500)    default null               comment '备注',
                                primary key (archive_id)
) engine=innodb comment = '电子健康档案表';

-- ----------------------------
-- 2. Health Data Table (Daily Checkups & Reports)
-- ----------------------------
drop table if exists health_data;
create table health_data (
                             data_id           bigint(20)      not null auto_increment    comment '数据ID',
                             user_id           bigint(20)      not null                   comment '患者ID',
                             height            double(5,2)     default null               comment '身高(cm)',
                             weight            double(5,2)     default null               comment '体重(kg)',
                             blood_pressure_sys int(3)         default null               comment '收缩压(mmHg)',
                             blood_pressure_dia int(3)         default null               comment '舒张压(mmHg)',
                             heart_rate        int(3)          default null               comment '心率(bpm)',
                             blood_sugar       double(4,2)     default null               comment '血糖(mmol/L)',
                             report_image      varchar(500)    default ''                 comment '体检报告图片URL',
                             measure_time      datetime        not null                   comment '测量时间',
                             create_time       datetime                                   comment '录入时间',
                             remark            varchar(500)    default null               comment '备注',
                             primary key (data_id)
) engine=innodb comment = '健康数据采集表';

-- ----------------------------
-- 3. Follow-up Plan Table
-- ----------------------------
drop table if exists followup_plan;
create table followup_plan (
                               plan_id           bigint(20)      not null auto_increment    comment '计划ID',
                               doctor_id         bigint(20)      not null                   comment '医生ID',
                               patient_id        bigint(20)      not null                   comment '患者ID',
                               plan_title        varchar(100)    not null                   comment '计划标题',
                               plan_content      varchar(2000)   default ''                 comment '随访内容/指导',
                               start_date        date            not null                   comment '开始日期',
                               end_date          date            not null                   comment '结束日期',
                               frequency         varchar(20)     default 'weekly'           comment '频率(daily, weekly, monthly)',
                               status            char(1)         default '0'                comment '状态(0进行中 1已完成 2已取消)',
                               create_by         varchar(64)     default ''                 comment '创建者',
                               create_time       datetime                                   comment '创建时间',
                               primary key (plan_id)
) engine=innodb comment = '随访计划表';

-- ----------------------------
-- 4. Follow-up Record Table
-- ----------------------------
drop table if exists followup_record;
create table followup_record (
                                 record_id         bigint(20)      not null auto_increment    comment '记录ID',
                                 plan_id           bigint(20)      not null                   comment '关联计划ID',
                                 patient_id        bigint(20)      not null                   comment '患者ID',
                                 doctor_id         bigint(20)      not null                   comment '医生ID',
                                 feedback          varchar(2000)   default ''                 comment '患者反馈',
                                 doctor_advice     varchar(2000)   default ''                 comment '医生建议/小结',
                                 record_date       date            not null                   comment '随访日期',
                                 status            char(1)         default '0'                comment '状态(0待随访 1已反馈 2已完成)',
                                 create_time       datetime                                   comment '创建时间',
                                 update_time       datetime                                   comment '更新时间',
                                 primary key (record_id)
) engine=innodb comment = '随访记录表';

-- ----------------------------
-- 5. Health Warning Table
-- ----------------------------
drop table if exists health_warning;
create table health_warning (
                                warning_id        bigint(20)      not null auto_increment    comment '预警ID',
                                user_id           bigint(20)      not null                   comment '患者ID',
                                warning_type      varchar(50)     not null                   comment '预警类型(血压、血糖等)',
                                warning_value     varchar(50)     not null                   comment '异常值',
                                warning_level     char(1)         default '1'                comment '预警等级(1低 2中 3高)',
                                warning_msg       varchar(500)    default ''                 comment '预警消息',
                                is_read           char(1)         default '0'                comment '是否已读(0否 1是)',
                                create_time       datetime                                   comment '预警时间',
                                primary key (warning_id)
) engine=innodb comment = '健康预警表';


-- ----------------------------
-- Insert Menus
-- ----------------------------
-- Health Management (Parent Menu) - Assuming ID 2005 to avoid conflict

-- Data rows omitted from the public source snapshot.


-- Health Archive

-- Data rows omitted from the public source snapshot.


-- Health Data (Collection & Visualization)

-- Data rows omitted from the public source snapshot.


-- Follow-up Management (Parent Menu)

-- Data rows omitted from the public source snapshot.


-- Follow-up Plan

-- Data rows omitted from the public source snapshot.


-- Follow-up Record

-- Data rows omitted from the public source snapshot.


-- Intelligent Warning

-- Data rows omitted from the public source snapshot.


-- ----------------------------
-- Insert Dictionaries
-- ----------------------------
-- Follow-up Status

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Follow-up Record Status

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.



-- ----------------------------
-- Test Data: Doctor and Patient Users (Password: 123456)
-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- ----------------------------
-- Assign Roles
-- ----------------------------
-- User 3 -> Role 3 (Doctor)

-- Data rows omitted from the public source snapshot.

-- User 4 -> Role 4 (Patient)

-- Data rows omitted from the public source snapshot.



-- ----------------------------
-- Grant Permissions to Roles
-- ----------------------------

-- Role 3: Doctor (Access to Patient Mgmt, Health Mgmt, Follow-up, Warnings)

-- Data rows omitted from the public source snapshot.
 -- Patient Mgmt

-- Data rows omitted from the public source snapshot.
 -- My Patients

-- Data rows omitted from the public source snapshot.
 -- Health Mgmt

-- Data rows omitted from the public source snapshot.
 -- Archive

-- Data rows omitted from the public source snapshot.
 -- Data

-- Data rows omitted from the public source snapshot.
 -- Follow-up

-- Data rows omitted from the public source snapshot.
 -- Plan

-- Data rows omitted from the public source snapshot.
 -- Record

-- Data rows omitted from the public source snapshot.
 -- Warning

-- Role 4: Patient (Access to Health Mgmt, Follow-up, Warnings)

-- Data rows omitted from the public source snapshot.
 -- Health Mgmt

-- Data rows omitted from the public source snapshot.
 -- Archive

-- Data rows omitted from the public source snapshot.
 -- Data

-- Data rows omitted from the public source snapshot.
 -- Follow-up

-- Data rows omitted from the public source snapshot.
 -- Plan

-- Data rows omitted from the public source snapshot.
 -- Record


-- Grant Button Permissions (Assuming standard buttons for simplicity, adding key ones)
-- In a real scenario, you'd add specific button IDs (1000+) to the role_menu table.
-- For this prototype, ensuring they see the menus is the priority.


-- ----------------------------
-- Sample Data: Doctor-Patient Binding
-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- ----------------------------
-- Sample Data: Health Archive (For Patient 4)
-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- ----------------------------
-- Sample Data: Health Data (For Patient 4 - Trends)
-- ----------------------------
-- Data point 1 (5 days ago)

-- Data rows omitted from the public source snapshot.


-- Data point 2 (3 days ago)

-- Data rows omitted from the public source snapshot.


-- Data point 3 (1 day ago)

-- Data rows omitted from the public source snapshot.


-- ----------------------------
-- Sample Data: Follow-up Plan (Doctor 3 -> Patient 4)
-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- ----------------------------
-- Sample Data: Follow-up Record (Linked to Plan above)
-- ----------------------------
-- Assuming the plan_id is 1 (auto-increment dependent, but likely 1 if clean DB)

-- Data rows omitted from the public source snapshot.


-- ----------------------------
-- Sample Data: Health Warning (For Patient 4)
-- ----------------------------

-- Data rows omitted from the public source snapshot.




-- ----------------------------
-- FORCE REFRESH: New Paths for Patient/Doctor Lists
-- ----------------------------
delete from sys_menu where menu_id in (2000, 2030, 2031);
delete from sys_role_menu where menu_id in (2000, 2030, 2031);

-- Patient Management (Parent) - Path: 'patients'

-- Data rows omitted from the public source snapshot.


-- Doctor Management (Parent) - Path: 'doctors'

-- Data rows omitted from the public source snapshot.


-- Doctor List - Component: 'doctorList/index'

-- Data rows omitted from the public source snapshot.


-- Re-grant Permissions

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.




ALTER TABLE sys_doctor_patient MODIFY COLUMN create_time DATETIME COMMENT '关联时间';

ALTER TABLE health_data MODIFY COLUMN blood_sugar DECIMAL(10,2);
ALTER TABLE health_data MODIFY COLUMN height DECIMAL(10,2);
ALTER TABLE health_data MODIFY COLUMN weight DECIMAL(10,2);

update sys_user
set password = '123456',
    pwd_update_date = sysdate(),
    update_time = sysdate()
where del_flag = '0';


alter table health_archive
    add column disease_type varchar(64) default '' comment '病种' after allergy_history,
    add column age_stage varchar(32) default '' comment '年龄阶段' after disease_type,
    add column risk_level varchar(32) default '' comment '风险等级' after age_stage,
    add column archive_tags varchar(255) default '' comment '档案标签' after risk_level,
    add column diagnosis_record text comment '诊断记录' after archive_tags,
    add column exam_report text comment '检查报告' after diagnosis_record,
    add column training_plan text comment '训练计划' after exam_report,
    add column medication_advice text comment '用药建议' after training_plan;

alter table health_data
    add column rehab_photo varchar(255) default '' comment '康复训练图片' after report_image;

alter table followup_plan
    add column module_type varchar(32) default 'measure' comment '模块类型' after plan_title,
    add column target_group varchar(255) default '' comment '目标分组' after module_type,
    add column cron_expression varchar(64) default '' comment 'Cron表达式' after target_group,
    add column reminder_description varchar(500) default '' comment '提醒说明' after cron_expression,
    add column notify_channel varchar(64) default 'websocket' comment '通知渠道' after reminder_description,
    add column require_feedback varchar(1) default 'N' comment '是否需要患者反馈' after notify_channel,
    add column require_photo varchar(1) default 'N' comment '是否需要训练拍照' after require_feedback;

alter table followup_record
    add column medication_result varchar(64) default '' comment '服药结果' after doctor_advice,
    add column rehab_photo varchar(255) default '' comment '康复训练照片' after medication_result;

ALTER TABLE health_warning
    ADD COLUMN threshold_range varchar(200) DEFAULT '' COMMENT '系统判定阈值依据' AFTER warning_msg,
    ADD COLUMN confirm_status char(1) DEFAULT '0' COMMENT '医生确认状态(0待确认 1已确认)' AFTER is_read,
    ADD COLUMN confirm_note varchar(500) DEFAULT '' COMMENT '医生复核说明' AFTER confirm_status,
    ADD COLUMN confirm_by varchar(64) DEFAULT '' COMMENT '复核医生' AFTER confirm_note,
    ADD COLUMN confirm_time datetime COMMENT '复核时间' AFTER confirm_by;

UPDATE health_archive
SET disease_type = CASE
                       WHEN IFNULL(disease_type, '') <> '' THEN disease_type
                       WHEN medical_history LIKE '%高血压%' OR diagnosis_record LIKE '%高血压%' OR medication_advice LIKE '%高血压%' THEN '高血压'
                       WHEN medical_history LIKE '%糖尿病%' OR diagnosis_record LIKE '%糖尿病%' OR medication_advice LIKE '%糖尿病%' THEN '糖尿病'
                       WHEN medical_history LIKE '%冠心病%' OR diagnosis_record LIKE '%冠心病%' THEN '冠心病'
                       WHEN medical_history LIKE '%脑卒中%' OR medical_history LIKE '%脑梗%' OR diagnosis_record LIKE '%脑卒中%' THEN '脑卒中'
                       WHEN training_plan IS NOT NULL AND training_plan <> '' THEN '骨科康复'
                       ELSE '慢病管理'
    END
WHERE IFNULL(disease_type, '') = '';

UPDATE health_archive ha
SET age_stage = '成人'
WHERE IFNULL(age_stage, '') = '';

UPDATE health_archive ha
SET risk_level = CASE
                     WHEN EXISTS (
                         SELECT 1 FROM health_warning hw
                         WHERE hw.user_id = ha.user_id AND hw.warning_level = '3'
                     ) THEN '高风险'
                     WHEN EXISTS (
                         SELECT 1 FROM health_warning hw
                         WHERE hw.user_id = ha.user_id AND hw.warning_level = '2'
                     ) THEN '中风险'
                     WHEN EXISTS (
                         SELECT 1 FROM health_warning hw
                         WHERE hw.user_id = ha.user_id AND hw.warning_level = '1'
                     ) THEN '低风险'
                     ELSE '低风险'
    END
WHERE IFNULL(risk_level, '') = '';

UPDATE health_archive
SET archive_tags = CONCAT_WS(',',
                             NULLIF(disease_type, ''),
                             NULLIF(age_stage, ''),
                             NULLIF(risk_level, ''),
                             CASE WHEN training_plan IS NOT NULL AND training_plan <> '' THEN '康复训练' ELSE NULL END
                   )
WHERE IFNULL(archive_tags, '') = '';

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


update sys_notice n
    join followup_plan p on n.remark = concat('followup-plan:', p.plan_id)
set n.remark = concat('followup-notice|planId:', p.plan_id, '|doctorId:', p.doctor_id, '|patientId:', p.patient_id)
where n.remark like 'followup-plan:%';

SET NAMES utf8mb4;


SET @demo_image = '/profile/upload/2026/04/13/wallhaven-21d9vy_1920x1080_20260413172959A001.png';

DELETE FROM followup_record
WHERE plan_id IN (2001, 2002, 2003, 2004, 2005, 2006, 2007)
   OR patient_id IN (110, 111, 112, 113, 114, 115, 116);

DELETE FROM followup_plan
WHERE plan_id IN (2001, 2002, 2003, 2004, 2005, 2006, 2007)
   OR patient_id IN (110, 111, 112, 113, 114, 115, 116);

DELETE FROM health_warning
WHERE warning_id IN (4001, 4002, 4003, 4004, 4005, 4006, 4007, 4008, 4009, 4010, 4011, 4012)
   OR user_id IN (110, 111, 112, 113, 114, 115, 116);

DELETE FROM health_data
WHERE data_id IN (1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009, 1010)
   OR user_id IN (110, 111, 112, 113, 114, 115, 116);

DELETE FROM health_archive
WHERE user_id IN (110, 111, 112, 113, 114, 115, 116);

DELETE FROM sys_doctor_patient
WHERE patient_id IN (110, 111, 112, 113, 114, 115, 116);

DELETE FROM sys_user_role
WHERE user_id IN (110, 111, 112, 113, 114, 115, 116);

DELETE FROM sys_user
WHERE user_id IN (110, 111, 112, 113, 114, 115, 116);

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- 保持自增ID不回退
ALTER TABLE health_data AUTO_INCREMENT = 1011;
ALTER TABLE health_warning AUTO_INCREMENT = 4013;
ALTER TABLE followup_plan AUTO_INCREMENT = 2008;
ALTER TABLE followup_record AUTO_INCREMENT = 3008;

-- ----------------------------
-- 1、健康数据采集补充体温字段
-- ----------------------------
SET @health_data_temperature_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'health_data'
      AND COLUMN_NAME = 'temperature'
);
SET @health_data_temperature_sql = IF(
        @health_data_temperature_exists = 0,
        'ALTER TABLE health_data ADD COLUMN temperature DECIMAL(4,1) DEFAULT NULL COMMENT ''体温(℃)'' AFTER weight',
        'SELECT 1'
                                   );
PREPARE stmt FROM @health_data_temperature_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE health_data
SET temperature = CASE
                      WHEN blood_sugar IS NOT NULL AND blood_sugar >= 9 THEN 37.2
                      WHEN blood_pressure_sys IS NOT NULL AND blood_pressure_sys >= 150 THEN 37.1
                      WHEN blood_pressure_sys IS NOT NULL AND blood_pressure_sys <= 100 THEN 36.3
                      ELSE 36.6
    END
WHERE temperature IS NULL;

-- ----------------------------
-- 2、随访记录补充提醒状态字段
-- ----------------------------
SET @followup_record_reminder_count_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'followup_record'
      AND COLUMN_NAME = 'reminder_count'
);
SET @followup_record_reminder_count_sql = IF(
        @followup_record_reminder_count_exists = 0,
        'ALTER TABLE followup_record ADD COLUMN reminder_count INT DEFAULT 0 COMMENT ''提醒次数'' AFTER rehab_photo',
        'SELECT 1'
                                          );
PREPARE stmt FROM @followup_record_reminder_count_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @followup_record_last_reminder_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'followup_record'
      AND COLUMN_NAME = 'last_reminder_time'
);
SET @followup_record_last_reminder_sql = IF(
        @followup_record_last_reminder_exists = 0,
        'ALTER TABLE followup_record ADD COLUMN last_reminder_time DATETIME DEFAULT NULL COMMENT ''最近提醒时间'' AFTER reminder_count',
        'SELECT 1'
                                         );
PREPARE stmt FROM @followup_record_last_reminder_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE followup_record fr
    LEFT JOIN followup_plan fp ON fr.plan_id = fp.plan_id
SET fr.reminder_count = CASE
                            WHEN fr.reminder_count IS NULL OR fr.reminder_count = 0 THEN 1
                            ELSE fr.reminder_count
    END,
    fr.last_reminder_time = COALESCE(fr.last_reminder_time, fr.create_time, NOW())
WHERE fp.module_type = 'medication'
  AND fr.status <> '2'
  AND (fr.medication_result IS NULL OR fr.medication_result = '');

-- ----------------------------
-- 3、补充查询性能索引
-- ----------------------------
SET @followup_record_plan_date_idx_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'followup_record'
      AND INDEX_NAME = 'idx_followup_record_plan_date'
);
SET @followup_record_plan_date_idx_sql = IF(
        @followup_record_plan_date_idx_exists = 0,
        'ALTER TABLE followup_record ADD INDEX idx_followup_record_plan_date (plan_id, record_date)',
        'SELECT 1'
                                         );
PREPARE stmt FROM @followup_record_plan_date_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @followup_record_reminder_idx_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'followup_record'
      AND INDEX_NAME = 'idx_followup_record_reminder'
);
SET @followup_record_reminder_idx_sql = IF(
        @followup_record_reminder_idx_exists = 0,
        'ALTER TABLE followup_record ADD INDEX idx_followup_record_reminder (status, medication_result, last_reminder_time)',
        'SELECT 1'
                                        );
PREPARE stmt FROM @followup_record_reminder_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- 4、补充“用药逾期提醒扫描”定时任务
-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- ----------------------------
-- 1、菜单与角色授权补充
-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- ----------------------------
-- 2、补充患者档案详情字段
-- ----------------------------
UPDATE health_archive
SET diagnosis_record = CASE
                           WHEN disease_type = '高血压' THEN '门诊诊断为原发性高血压，近期以晨起血压偏高为主，需持续居家监测。'
                           WHEN disease_type = '糖尿病' THEN '门诊诊断为2型糖尿病，空腹血糖波动明显，建议规范饮食并坚持监测。'
                           WHEN disease_type = '冠心病' THEN '既往冠心病病史，偶有胸闷，建议定期复查心电图与血脂。'
                           WHEN disease_type = '脑卒中' THEN '脑卒中恢复期，存在步态不稳与上肢活动受限表现。'
                           WHEN disease_type = '慢阻肺' THEN '慢阻肺稳定期，活动后气促明显，建议规范吸入治疗。'
                           WHEN disease_type = '骨科康复' THEN '术后骨科康复阶段，重点观察关节活动度与疼痛恢复情况。'
                           ELSE '慢病综合管理中，需结合血压、血糖和症状变化进行持续评估。'
    END
WHERE diagnosis_record IS NULL OR diagnosis_record = '';

UPDATE health_archive
SET exam_report = CASE
                      WHEN disease_type = '高血压' THEN '最近一次检查显示收缩压控制欠稳定，建议继续早晚测压并记录波动趋势。'
                      WHEN disease_type = '糖尿病' THEN '最近一次复查提示空腹血糖偏高，需继续关注饮食与运动管理。'
                      WHEN disease_type = '冠心病' THEN '近期复查显示心功能基本稳定，但仍需关注胸闷胸痛变化。'
                      WHEN disease_type = '脑卒中' THEN '康复评估提示下肢肌力恢复一般，平衡能力仍需强化训练。'
                      WHEN disease_type = '慢阻肺' THEN '肺功能提示轻中度通气障碍，建议继续呼吸训练。'
                      WHEN disease_type = '骨科康复' THEN '康复评估显示关节活动度较前改善，建议继续循序渐进训练。'
                      ELSE '阶段性复查结果整体平稳，建议结合随访计划持续观察。'
    END
WHERE exam_report IS NULL OR exam_report = '';

UPDATE health_archive
SET training_plan = CASE
                        WHEN disease_type = '脑卒中' THEN '每日进行站立平衡训练、上肢抬举训练各2组，完成后记录执行感受。'
                        WHEN disease_type = '骨科康复' THEN '每日晚间完成关节屈伸训练和步态训练，训练后上传康复照片留档。'
                        WHEN disease_type = '慢阻肺' THEN '每日完成腹式呼吸训练和缩唇呼吸训练，每次10分钟。'
                        ELSE '结合病种特点进行低强度康复或日常活动训练，按计划完成后反馈执行情况。'
    END
WHERE training_plan IS NULL OR training_plan = '';

UPDATE health_archive
SET medication_advice = CASE
                            WHEN disease_type = '高血压' THEN '按医嘱每日规律服用降压药，晨起服药后30分钟内完成血压测量。'
                            WHEN disease_type = '糖尿病' THEN '按时服用降糖药物，保持三餐规律，逾期服药需及时反馈。'
                            WHEN disease_type = '冠心病' THEN '遵医嘱服用抗血小板及调脂药物，如有胸闷加重应及时就医。'
                            WHEN disease_type = '脑卒中' THEN '按时服用二级预防药物，若出现头晕、乏力加重需及时复诊。'
                            WHEN disease_type = '慢阻肺' THEN '规范使用吸入药物，注意呼吸困难与咳痰变化。'
                            WHEN disease_type = '骨科康复' THEN '按医嘱服用止痛及辅助恢复药物，训练前后注意观察疼痛变化。'
                            ELSE '按计划完成晚间集中用药，并同步反馈服药结果。'
    END
WHERE medication_advice IS NULL OR medication_advice = '';

UPDATE health_archive
SET archive_tags = CASE
                       WHEN archive_tags IS NULL OR archive_tags = '' THEN CONCAT_WS('，', disease_type, age_stage, risk_level)
                       ELSE archive_tags
    END
WHERE archive_tags IS NULL OR archive_tags = '';

-- 为存在患者角色但没有档案的用户补一条基础档案

-- Data rows omitted from the public source snapshot.


-- ----------------------------
-- 3、补充随访通知演示数据
-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- ----------------------------
-- 1、菜单按钮权限
-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- ----------------------------
-- 2、补充管理员可见的演示随访通知
-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


UPDATE health_archive
SET archive_tags = REPLACE(REPLACE(REPLACE(IFNULL(archive_tags, ''), ',', '，'), ' ', ''), '、', '，')
WHERE archive_tags IS NOT NULL
  AND archive_tags <> '';

UPDATE health_archive
SET archive_tags = TRIM(BOTH '，' FROM REPLACE(REPLACE(REPLACE(CONCAT('，', archive_tags, '，'), '，，', '，'), '，，', '，'), '，，', '，'))
WHERE archive_tags IS NOT NULL
  AND archive_tags <> '';

UPDATE health_archive
SET archive_tags = CONCAT_WS('，',
                             NULLIF(disease_type, ''),
                             NULLIF(age_stage, ''),
                             NULLIF(risk_level, '')
                   )
WHERE archive_tags IS NULL
   OR archive_tags = '';

UPDATE health_archive
SET archive_tags = CONCAT_WS('，',
                             NULLIF(disease_type, ''),
                             NULLIF(age_stage, ''),
                             NULLIF(risk_level, ''),
                             NULLIF(archive_tags, '')
                   )
WHERE archive_tags IS NOT NULL
  AND archive_tags <> ''
  AND (
    archive_tags NOT LIKE CONCAT('%', disease_type, '%')
        OR archive_tags NOT LIKE CONCAT('%', age_stage, '%')
        OR archive_tags NOT LIKE CONCAT('%', risk_level, '%')
    );

UPDATE health_archive
SET archive_tags = REPLACE(REPLACE(REPLACE(CONCAT('，', archive_tags, '，'), '，，', '，'), '，，', '，'), '，，', '，')
WHERE archive_tags IS NOT NULL
  AND archive_tags <> '';

UPDATE health_archive
SET archive_tags = TRIM(BOTH '，' FROM archive_tags)
WHERE archive_tags IS NOT NULL
  AND archive_tags <> '';

SET @doctor_id = COALESCE(
        (SELECT user_id FROM sys_user WHERE user_id = 3 LIMIT 1),
        (
            SELECT ur.user_id
            FROM sys_user_role ur
                     INNER JOIN sys_role r ON r.role_id = ur.role_id AND r.role_key = 'doctor'
            ORDER BY ur.user_id
            LIMIT 1
        )
                 );

SET @patient_id = COALESCE(
        (SELECT user_id FROM sys_user WHERE user_id = 4 LIMIT 1),
        (SELECT user_id FROM sys_user WHERE user_id = 115 LIMIT 1),
        (SELECT user_id FROM sys_user WHERE user_id = 113 LIMIT 1),
        (
            SELECT ur.user_id
            FROM sys_user_role ur
                     INNER JOIN sys_role r ON r.role_id = ur.role_id AND r.role_key = 'patient'
            ORDER BY ur.user_id
            LIMIT 1
        )
                  );

SET @doctor_name = (
    SELECT nick_name
    FROM sys_user
    WHERE user_id = @doctor_id
    LIMIT 1
);

SET @patient_name = (
    SELECT nick_name
    FROM sys_user
    WHERE user_id = @patient_id
    LIMIT 1
);

-- Data rows omitted from the public source snapshot.


SET @plan_title = '康复训练反馈拍照联合提醒';
SET @existing_plan_id = (
    SELECT plan_id
    FROM followup_plan
    WHERE plan_title = @plan_title
      AND doctor_id = @doctor_id
      AND patient_id = @patient_id
    LIMIT 1
);
SET @next_plan_id = (
    SELECT IFNULL(MAX(plan_id), 2000) + 1
    FROM followup_plan
);

-- Data rows omitted from the public source snapshot.


UPDATE followup_plan
SET doctor_id = @doctor_id,
    patient_id = @patient_id,
    module_type = 'rehab',
    target_group = '康复训练/重点反馈/拍照留档',
    cron_expression = '0 30 19 * * ?',
    reminder_description = '每晚19:30提醒患者完成康复训练，提交执行反馈并上传训练照片。',
    notify_channel = 'all',
    require_feedback = 'Y',
    require_photo = 'Y',
    plan_content = CONCAT('请患者【', IFNULL(@patient_name, '演示患者'), '】完成当日康复训练，反馈训练感受、症状变化，并上传训练照片留档。'),
    start_date = CURDATE(),
    end_date = DATE_ADD(CURDATE(), INTERVAL 60 DAY),
    frequency = 'daily',
    status = '0'
WHERE plan_id = IFNULL(@existing_plan_id, @next_plan_id);

SET @final_plan_id = (
    SELECT plan_id
    FROM followup_plan
    WHERE plan_title = @plan_title
      AND doctor_id = @doctor_id
      AND patient_id = @patient_id
    LIMIT 1
);

SET @existing_record_id = (
    SELECT record_id
    FROM followup_record
    WHERE plan_id = @final_plan_id
      AND record_date = CURDATE()
    LIMIT 1
);
SET @next_record_id = (
    SELECT IFNULL(MAX(record_id), 3000) + 1
    FROM followup_record
);

-- Data rows omitted from the public source snapshot.


UPDATE followup_record
SET patient_id = @patient_id,
    doctor_id = @doctor_id,
    feedback = '',
    doctor_advice = '请填写训练完成情况，并上传一张康复训练照片。',
    medication_result = '',
    rehab_photo = '',
    status = '0',
    update_time = NOW()
WHERE record_id = IFNULL(@existing_record_id, @next_record_id);

SELECT
    @doctor_id     AS doctor_id,
    @doctor_name   AS doctor_name,
    @patient_id    AS patient_id,
    @patient_name  AS patient_name,
    @final_plan_id AS plan_id,
    IFNULL(@existing_record_id, @next_record_id) AS record_id,
    @plan_title    AS plan_title;
